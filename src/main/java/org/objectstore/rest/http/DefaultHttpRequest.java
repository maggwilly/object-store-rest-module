package org.objectstore.rest.http;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.domain.HttpProtocol;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.BaseHttpMessage;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;

import java.net.URI;
import java.util.Collection;

public class DefaultHttpRequest extends BaseHttpMessage implements HttpRequest {
    private final URI uri;
    private final String path;
    private final String method;
    private HttpProtocol version;
    private final MultiMap<String, String> headers;
    private final MultiMap<String, String> queryParams;
    private final HttpEntity entity;

     DefaultHttpRequest(URI uri, String path, String method, MultiMap<String, String> headers, MultiMap<String, String> queryParams, HttpEntity entity) {
        this.uri = uri;
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.queryParams = queryParams;
        this.entity = entity;
    }

    public HttpProtocol getProtocol() {
        return this.version;
    }

    public String getPath() {
        return this.path;
    }

    public String getMethod() {
        return this.method;
    }

    public Collection<String> getHeaderNames() {
        return this.headers.keySet();
    }

    public String getHeaderValue(String headerName) {
        return (String)this.headers.get(headerName);
    }

    public Collection<String> getHeaderValues(String headerName) {
        return this.headers.getAll(headerName);
    }

    public MultiMap<String, String> getHeaders() {
        return this.headers.toImmutableMultiMap();
    }

    public HttpEntity getEntity() {
        return this.entity;
    }

    public URI getUri() {
        return this.uri;
    }

    public MultiMap<String, String> getQueryParams() {
        return this.queryParams.toImmutableMultiMap();
    }

    public String toString() {
        return "DefaultHttpRequest {" + System.lineSeparator() + "  uri: " + this.uri.toString() + "," + System.lineSeparator() + "  path: " + this.path + "," + System.lineSeparator() + "  method: " + this.method + "," + System.lineSeparator() + "  headers: " + this.headers.toString() + "," + System.lineSeparator() + "  queryParams: " + this.queryParams.toString() + System.lineSeparator() + "}";
    }
}