package org.objectstore.rest.internal.connection.params;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class httpConnectionParams {
    @Parameter
    @Example("https://api.rest-objectstore.edf.fr")
    private String url;
    @Parameter
    @Optional
    private MultiMap<String, String> headers = MultiMap.emptyMultiMap();

    @Parameter
    @Optional(defaultValue = "2000")
    @Placement(tab = "Advanced", order = 3)
    private int connectionTimeout;
    @Parameter
    @Optional( defaultValue = "MILLISECONDS")
    @Placement(tab = "Advanced",order = 4
    )
    private TimeUnit connectionTimeUnit;

    @Parameter
    @Optional
    @Placement(tab = "Connection", order = 6)
    private Integer entryTTL;
    public String getUrl() {
        return url;
    }

    public httpConnectionParams setUrl(String url) {
        this.url = url;
        return this;
    }

    public MultiMap<String, String> getHeaders() {
        return headers;
    }

    public httpConnectionParams setHeaders(MultiMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public httpConnectionParams setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public TimeUnit getConnectionTimeUnit() {
        return connectionTimeUnit;
    }

    public httpConnectionParams setConnectionTimeUnit(TimeUnit connectionTimeUnit) {
        this.connectionTimeUnit = connectionTimeUnit;
        return this;
    }

    public Integer getEntryTTL() {
        return entryTTL;
    }

    public httpConnectionParams setEntryTTL(Integer entryTTL) {
        this.entryTTL = entryTTL;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        httpConnectionParams that = (httpConnectionParams) o;
        return connectionTimeout == that.connectionTimeout && url.equals(that.url) && headers.equals(that.headers) && connectionTimeUnit == that.connectionTimeUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, headers, connectionTimeout, connectionTimeUnit);
    }
}
