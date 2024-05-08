package org.objectstore.rest.http;

import org.objectstore.rest.internal.connection.params.httpConnectionParams;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.core.api.lifecycle.LifecycleUtils;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

public class HttpConnection implements Startable, Stoppable {

    private final HttpRequesterConnectionManager.ShareableHttpClient httpClient;
    private final HttpRequestAuthentication authentication;
    private final httpConnectionParams connectionParams;

    public HttpConnection(HttpRequesterConnectionManager.ShareableHttpClient httpClient, HttpRequestAuthentication authentication, httpConnectionParams connectionParams) {
        this.httpClient = httpClient;
        this.authentication = authentication;
        this.connectionParams= connectionParams;
    }
    public HttpRequestAuthentication getDefaultAuthentication() {
        return this.authentication;
    }
    @Override
    public void start() throws MuleException {
        this.httpClient.start();
        try {
            LifecycleUtils.startIfNeeded(this.authentication);
        } catch (Exception var2) {
            this.httpClient.stop();
            throw var2;
        }
    }

    public httpConnectionParams getConnectionParams() {
        return connectionParams;
    }

    @Override
    public void stop() throws MuleException {
        LifecycleUtils.stopIfNeeded(this.authentication);
        this.httpClient.stop();
    }


    public CompletableFuture<HttpResponse> send(HttpRequest request, boolean followRedirects, HttpAuthentication authentication) {
        int responseTimeout = (int) connectionParams.getConnectionTimeUnit().toMillis(connectionParams.getConnectionTimeout());
        return this.httpClient.sendAsync(request, responseTimeout, followRedirects, authentication);
    }
}
