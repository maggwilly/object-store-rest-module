package org.objectstore.rest.http;

import java.net.URI;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.api.util.Preconditions;
import org.mule.runtime.http.api.HttpConstants.Method;
import org.mule.runtime.http.api.domain.CaseInsensitiveMultiMap;
import org.mule.runtime.http.api.domain.message.HttpMessageBuilder;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.utils.UriCache;

public final class HttpRequestBuilder extends HttpMessageBuilder<HttpRequestBuilder, HttpRequest> {
    private String path;
    private URI uri;
    private String method;
    private MultiMap<String, String> queryParams;

    public HttpRequestBuilder(boolean preserveHeadersCase) {
        this.method = Method.GET.name();
        this.queryParams = new MultiMap();
        this.headers = new CaseInsensitiveMultiMap(!preserveHeadersCase);
    }

    public HttpRequestBuilder uri(String uri) {
        int queryPos = uri.indexOf("?");
        this.path = queryPos > -1 ? uri.substring(0, queryPos) : uri;
        this.uri = UriCache.getUriFromString(uri);
        return this;
    }

    public HttpRequestBuilder uri(URI uri) {
        this.uri = uri;
        return this;
    }

    public HttpRequestBuilder method(String method) {
        this.method = method;
        return this;
    }

    public HttpRequestBuilder method(Method method) {
        this.method = method.name();
        return this;
    }

    public HttpRequestBuilder queryParams(MultiMap<String, String> queryParams) {
        this.queryParams.putAll(queryParams);
        return this;
    }

    public HttpRequestBuilder addQueryParam(String name, String value) {
        this.queryParams.put(name, value);
        return this;
    }

    public URI getUri() {
        return this.uri;
    }

    public String getMethod() {
        return this.method;
    }

    public MultiMap<String, String> getQueryParams() {
        return this.queryParams.toImmutableMultiMap();
    }

    public HttpRequest build() {
        Preconditions.checkNotNull(this.uri, "URI must be specified to create an HTTP request");
        return new DefaultHttpRequest(this.uri, this.path, this.method, this.headers.toImmutableMultiMap(), this.queryParams.toImmutableMultiMap(), this.entity);
    }
}

