package com.jelly.eatme.places.repository.service.client.impl;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.jelly.eatme.places.repository.service.ServiceException;
import com.jelly.eatme.places.repository.service.client.IHttpServiceClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpServiceClient implements IHttpServiceClient {

    private static final Logger Log = LoggerFactory.getLogger(HttpServiceClient.class.getSimpleName());

    private HttpRequestFactory factory;
    private HttpParams parameters;

    @Override
    public void initialize() {
        this.parameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        HttpConnectionParams.setConnectionTimeout(this.parameters, 60000);
        // Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
        HttpConnectionParams.setSoTimeout(this.parameters, 60000);
        // Create a new HttpClient and Post Header
        this.factory = createRequestFactory(new NetHttpTransport());
    }

    @Override
    public <HttpResponse> HttpResponse invoke(HttpRequest request, Class<HttpResponse> type) throws ServiceException {
        return (HttpResponse) this.invoke(request);
    }

    @Override
    public HttpResponse invoke(HttpRequest request) throws ServiceException {
        try {
            return request.execute();
        } catch (IOException e) {
            throw new ServiceException("Http client request failed.", e);
        }
    }

    @Override
    public void shutdown() {
        HttpClientFactory.shutdown();
    }

    @Override
    public HttpRequest buildHttpRequest(String method, String uri) {
        try {
            Log.info("Building http request method='{}' to URI='{}'", method, uri);
            return this.factory.buildRequest(method, new GenericUrl(uri), null);
        } catch (IOException e) {
            Log.error("This shouldn't have happened: error building request", e);
        }
        return null;
    }

    public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
        return transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                HttpHeaders headers = new HttpHeaders();
                headers.setUserAgent("Android-EatMe-Places");
                request.setHeaders(headers);
                JsonObjectParser parser = new JsonObjectParser(new JacksonFactory());
                request.setParser(parser);
            }
        });
    }

}