package com.jelly.eatme.places.repository.service.client;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.jelly.eatme.places.repository.service.ServiceException;

public interface IHttpServiceClient extends IServiceClient<HttpRequest> {

    HttpRequest buildHttpRequest(String method, String uri);

    HttpResponse invoke(HttpRequest request) throws ServiceException;

    @Override
    <HttpResponse> HttpResponse invoke(HttpRequest request, Class<HttpResponse> type) throws ServiceException;

}