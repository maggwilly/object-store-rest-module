package org.objectstore.rest.http;

import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.util.Preconditions;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpRequesterConnectionManager implements Disposable {
    @Inject
    private HttpService httpService;
    private Map<String, ShareableHttpClient> clients = new HashMap();

    public HttpRequesterConnectionManager() {
    }

    public HttpRequesterConnectionManager(HttpService httpService) {
        this.httpService = httpService;
    }

    public Optional<ShareableHttpClient> lookup(String configName) {
        return Optional.ofNullable(this.clients.get(configName));
    }

    public synchronized ShareableHttpClient create(String configName, HttpClientConfiguration clientConfiguration) {
        Preconditions.checkArgument(!this.clients.containsKey(configName), String.format("There's an HttpClient available for %s already.", configName));
        ShareableHttpClient client = new ShareableHttpClient(this.httpService.getClientFactory().create(clientConfiguration));
        this.clients.put(configName, client);
        return client;
    }

    public void dispose() {
        this.clients.clear();
    }

    public class ShareableHttpClient {
        private HttpClient delegate;
        private AtomicInteger usageCount = new AtomicInteger(0);

        ShareableHttpClient(HttpClient client) {
            this.delegate = client;
        }

        public void start() {
            if (this.usageCount.incrementAndGet() == 1) {
                this.delegate.start();
            }

        }

        public void stop() {
            if (this.usageCount.decrementAndGet() == 0) {
                this.delegate.stop();
            }

        }

        public CompletableFuture<HttpResponse> sendAsync(HttpRequest request, int responseTimeout, boolean followRedirects, HttpAuthentication authentication) {
            return this.delegate.sendAsync(request, responseTimeout, followRedirects, authentication);
        }
    }
}

