package org.objectstore.rest.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.mule.runtime.api.serialization.SerializationException;
import org.objectstore.rest.http.HttpConnection;
import org.objectstore.rest.http.HttpRequestBuilder;
import org.objectstore.rest.http.HttpUtils;
import org.objectstore.rest.internal.connection.params.httpConnectionParams;
import org.objectstore.rest.internal.exception.InvalidDataException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.i18n.I18nMessageFactory;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.serialization.ObjectSerializer;
import org.mule.runtime.api.store.ObjectStoreException;
import org.mule.runtime.api.store.ObjectStoreSettings;
import org.mule.runtime.api.store.TemplateObjectStore;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.entity.EmptyHttpEntity;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RestObjectStore extends TemplateObjectStore<Serializable> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestObjectStore.class);
    public static final String VALUE_KEY = "value";
    private final HttpConnection httpConnection;
    private final Integer maxEntries;
    private final long expirationInterval;
    private final Integer entryTTL;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String name;
    private ObjectSerializer objectSerializer;
    private Scheduler scheduler;
    private ScheduledFuture<?> scheduledTask;

    public RestObjectStore(HttpConnection httpConnection, ObjectSerializer serializer, ObjectStoreSettings settings, Scheduler scheduler, String name) {
        Integer entryTTL = httpConnection.getConnectionParams().getEntryTTL();
        Integer ttl = settings.getEntryTTL().map(Long::intValue).orElse(entryTTL);
        if (ttl == null) {
            this.entryTTL = null;
        } else {
            this.entryTTL = ttl / 1000;
            if (entryTTL != null && this.entryTTL > entryTTL) {
                throw new InvalidDataException("The entry TTL set for the custom object store is greater than the one set in the Connection configuration");
            }
        }
        this.name = name;
        this.maxEntries = settings.getMaxEntries().orElse(null);
        this.httpConnection = httpConnection;
        this.expirationInterval = settings.getExpirationInterval();
        this.objectSerializer = serializer;
        this.scheduler = scheduler;
    }

    @Override
    protected boolean doContains(String s) {
        return Objects.nonNull(doRetrieve(s));
    }

    @Override
    protected void doStore(String key, Serializable serializable) {
        LOGGER.info("Storing : {} - {}", key, serializable);
        httpConnectionParams connectionParams = httpConnection.getConnectionParams();
        HttpRequestBuilder httpRequestBuilder = new HttpRequestBuilder(true);
        TypedValue<Serializable> typedValue = new TypedValue<>(serializable, DataType.JSON_STRING);
        HttpEntity requestEntity = createRequestEntity(httpRequestBuilder, typedValue);
        HttpRequest httpRequest = httpRequestBuilder.uri(getUri(key)).method(HttpConstants.Method.POST).entity(requestEntity).headers(connectionParams.getHeaders()).build();
        try {
            HttpResponse httpResponse = doSent(httpRequest).get();
            LOGGER.info("Store response: {}", httpResponse.getEntity());
        } catch (Exception e) {
            LOGGER.error("Failed to store value {}.", key, e);
            throw new RuntimeException(e);
        }
    }

    public Integer getMaxEntries() {
        return this.maxEntries;
    }

    private String getUri(String key) {
        httpConnectionParams connectionParams = httpConnection.getConnectionParams();
        return connectionParams.getUrl() + "/" + key;
    }

    private byte[] toByteArray(Serializable serializable) {
        LOGGER.info("Serializable: {}", serializable);
        Map<String, Serializable> body = new HashMap<>();
        body.put("value", serializable);
        Gson gson = new Gson();
        return this.objectSerializer.getInternalProtocol().serialize(body);
    }

    private Serializable fromByteArray(byte[] bytes) {
        if (bytes.length == 0) {
            return null;
        }
        try {
            String deserialize = this.objectSerializer.getInternalProtocol().deserialize(bytes);
            Gson gson = new Gson();
            //Map<String, ? extends Serializable> map = gson.fromJson(deserialize, Map.class);
            LOGGER.info("fromByteArray: {}", deserialize);
            return deserialize;
        } catch (SerializationException e){
            return null;
        }
    }

    private HttpEntity createRequestEntity(HttpRequestBuilder httpRequestBuilder, TypedValue<Serializable> body) {
        Serializable payload = body.getValue();
        MediaType mediaType = body.getDataType().getMediaType();
        httpRequestBuilder.addHeader(org.objectstore.rest.http.HttpConstants.CONTENT_TYPE_HEADER, mediaType.toRfcString());
        return new ByteArrayHttpEntity(this.toByteArray(payload));
    }

    @Override
    protected Serializable doRetrieve(String key) {
        LOGGER.info("Retrieving : {}", key);
        httpConnectionParams connectionParams = httpConnection.getConnectionParams();
        HttpRequestBuilder httpRequestBuilder = new HttpRequestBuilder(true);
        HttpEntity requestEntity = new EmptyHttpEntity();
        HttpRequest httpRequest = httpRequestBuilder.uri(getUri(key)).method(HttpConstants.Method.GET).entity(requestEntity).headers(connectionParams.getHeaders()).build();
        try {
            HttpResponse httpResponse = doSent(httpRequest).get();
            LOGGER.info("Retrieve response: {}", httpResponse.getEntity());
            HttpEntity entity = httpResponse.getEntity();
            return fromByteArray(entity.getBytes());
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve value {}.", key, e);
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<HttpResponse> doSent(HttpRequest httpRequest) {
        return httpConnection.send(httpRequest, false, HttpUtils.resolveAuthentication(httpConnection.getDefaultAuthentication()));
    }

    @Override
    protected Serializable doRemove(String key) {
        httpConnectionParams connectionParams = httpConnection.getConnectionParams();
        HttpRequestBuilder httpRequestBuilder = new HttpRequestBuilder(true);
        HttpEntity requestEntity = new EmptyHttpEntity();
        HttpRequest httpRequest = httpRequestBuilder.uri(getUri(key)).method(HttpConstants.Method.DELETE).entity(requestEntity).headers(connectionParams.getHeaders()).build();
        try {
            HttpResponse httpResponse = doSent(httpRequest).get();
            LOGGER.info("response: {}", httpResponse);
            HttpEntity entity = httpResponse.getEntity();
            return fromByteArray(entity.getBytes());
        } catch (Exception e) {
            LOGGER.error("Failed to remove value {}.", key, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void clear() {
        LOGGER.warn("Not implemented");
    }

    public void open() throws ObjectStoreException {
        startHttpConnection();
        if (this.expirationInterval > 0L) {
            try {
                RedisObjectStoreExpiryTask storeExpiryTask = new RedisObjectStoreExpiryTask(this);
                this.scheduledTask = this.scheduler.scheduleWithFixedDelay(storeExpiryTask, 0L, this.expirationInterval, TimeUnit.MILLISECONDS);
            } catch (Exception var2) {
                throw new ObjectStoreException(I18nMessageFactory.createStaticMessage("ObjectStore expiry task could not be scheduled for object store: " + this.name), var2);
            }
        }
    }

    private void startHttpConnection() {
        try {
            httpConnection.start();
        } catch (MuleException e) {
            LOGGER.error("Failed to start http connection. ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            httpConnection.stop();
        } catch (MuleException e) {
            LOGGER.error("Failed to close http connection. ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> allKeys() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Serializable> retrieveAll() {
        LOGGER.warn("Not implemented");
        return new HashMap<>();
    }
}
