package org.objectstore.rest.http;

import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.authentication.UsernamePasswordAuthentication;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;

public class HttpUtils {
    public static HttpAuthentication resolveAuthentication(HttpRequestAuthentication authentication) {
        HttpAuthentication requestAuthentication = null;
        if (authentication instanceof UsernamePasswordAuthentication) {
            requestAuthentication = (HttpAuthentication) authentication;
        }
        return requestAuthentication;
    }
}
