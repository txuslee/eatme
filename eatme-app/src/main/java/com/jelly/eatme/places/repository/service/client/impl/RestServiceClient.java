package com.jelly.eatme.places.repository.service.client.impl;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.jelly.eatme.places.repository.service.ServiceException;
import com.jelly.eatme.places.repository.service.client.IHttpServiceClient;
import com.jelly.eatme.places.repository.service.client.IRestServiceClient;
import com.jelly.eatme.places.repository.service.request.IServiceRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RestServiceClient implements IRestServiceClient {

    private static final Logger Log = LoggerFactory.getLogger(RestServiceClient.class.getSimpleName());

    private final IHttpServiceClient client;

    public RestServiceClient() {
        this(new HttpServiceClient());
    }

    public RestServiceClient(IHttpServiceClient client) {
        this.client = client;
    }

    @Override
    public void initialize() {
        this.client.initialize();
    }

    protected HttpRequest buildHttpRequest(IServiceRequest request) {
        HttpRequest httpRequest = this.client.buildHttpRequest(String.valueOf(request.getMethod()), request.getUri());
        return httpRequest;
    }

    @Override
    public <R> R invoke(IServiceRequest request, Class<R> type) throws ServiceException {
        HttpEntity entity = null;
        R result = null;
        try {
            final HttpRequest httpRequest = this.buildHttpRequest(request);
            final HttpResponse response = this.client.invoke(httpRequest);
            // This is an inline implementation of the behaviour linked to the HTTP response code.
            // Depending on the amount of 'status codes' to manage it would be desirable to
            // extend this functionality into some kind of listener (success/failure) or handler
            // class with more complex logic.
            final int statusCode = response.getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                Log.debug("Successful response received");
                result = response.parseAs(type);
                Log.debug("Successfully parsed to '{}'", type);
            } else if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
                HttpException exception = null;
                if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    exception = new AuthenticationException("Authorization Required");
                }
                throw new ServiceException(String.format("[%d] Bad HTTP response", statusCode), exception);
            }
        } catch (IOException e) {
            throw new ServiceException("RestServiceClient was unable to extract message body.", e);
        }
        return result;
    }

    @Override
    public void shutdown() {
        this.client.shutdown();
    }

}