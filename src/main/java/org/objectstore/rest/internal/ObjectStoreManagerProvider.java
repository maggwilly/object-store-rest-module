package org.objectstore.rest.internal;

import org.objectstore.rest.http.HttpConnection;
import org.objectstore.rest.http.HttpRequesterConnectionManager;
import org.objectstore.rest.internal.connection.params.httpConnectionParams;
import org.objectstore.rest.internal.stereotype.ObjectStoreConnectionStereotype;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.scheduler.SchedulerConfig;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.serialization.ObjectSerializer;
import org.mule.runtime.api.store.ObjectStoreManager;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.core.api.lifecycle.LifecycleUtils;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.stereotype.Stereotype;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
@Alias("http")
@DisplayName("Http rest")
@Stereotype(ObjectStoreConnectionStereotype.class)
public class ObjectStoreManagerProvider   implements CachedConnectionProvider<ObjectStoreManager>, Initialisable {
  private final Logger LOGGER = LoggerFactory.getLogger(ObjectStoreManagerProvider.class);
  private static final String NAME_PATTERN = "http.requester.%s";

  @RefName
  private String configName;
  @Inject
  private SchedulerService schedulerService;
  @Inject
  private HttpRequesterConnectionManager connectionManager;
  @Inject
  @Named("_muleSchedulerBaseConfig")
  private SchedulerConfig schedulerConfig;
  @ParameterGroup(name = "Connection")
  @Placement(order = 1)
  private httpConnectionParams connectionParams;
  @Parameter
  @Placement(tab = "Security", order = 2)
  @Optional
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @DisplayName("TLS Configuration")
  private TlsContextFactory tlsContext;
  @Parameter
  @Placement(tab = "Security",order = 1)
  @Optional
  @DisplayName("Authentication")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private HttpRequestAuthentication authentication;
  @Inject
  @Named("_muleDefaultObjectSerializer")
  private ObjectSerializer serializer;

    @Override
    public ObjectStoreManager connect() {
      java.util.Optional<HttpRequesterConnectionManager.ShareableHttpClient> client = this.connectionManager.lookup(this.configName);
      HttpRequesterConnectionManager.ShareableHttpClient httpClient = client.orElseGet(() -> this.connectionManager.create(this.configName, this.getHttpClientConfiguration()));
      HttpConnection httpConnection = new HttpConnection(httpClient, authentication, this.connectionParams);
        return new RestObjectStoreManager(httpConnection, this.serializer, this.schedulerService.customScheduler(this.schedulerConfig.withName(this.configName + "-Monitor").withMaxConcurrentTasks(1)));
    }

  public void initialise() throws InitialisationException {
    LifecycleUtils.initialiseIfNeeded(tlsContext);
    LifecycleUtils.initialiseIfNeeded(authentication);
  }

    @Override
    public void disconnect(ObjectStoreManager connection) {

    }
  private HttpClientConfiguration getHttpClientConfiguration() {
    String name = String.format(NAME_PATTERN, configName);
    return new HttpClientConfiguration.Builder()
            .setTlsContextFactory(tlsContext)
            .setMaxConnections(-1)
            .setUsePersistentConnections(false)
            .setConnectionIdleTimeout(30000)
            .setName(name)
            .build();
  }
    @Override
    public ConnectionValidationResult validate(ObjectStoreManager connection) {
        return success();
    }

  public httpConnectionParams getConnectionParams() {
    return connectionParams;
  }

  public ObjectStoreManagerProvider setConnectionParams(httpConnectionParams connectionParams) {
    this.connectionParams = connectionParams;
    return this;
  }

  public TlsContextFactory getTlsContext() {
    return tlsContext;
  }

  public ObjectStoreManagerProvider setTlsContext(TlsContextFactory tlsContext) {
    this.tlsContext = tlsContext;
    return this;
  }

  public HttpRequestAuthentication getAuthentication() {
    return authentication;
  }

  public ObjectStoreManagerProvider setAuthentication(HttpRequestAuthentication authentication) {
    this.authentication = authentication;
    return this;
  }
}
