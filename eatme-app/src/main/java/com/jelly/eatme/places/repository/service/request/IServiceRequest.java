package com.jelly.eatme.places.repository.service.request;

import com.jelly.eatme.places.repository.service.ServiceException;

public interface IServiceRequest {

    public enum Method {
        GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, PATCH
    }

    String getContentType();

    Method getMethod();

    String getAccept();

    String getUri();

    String build(String... values) throws ServiceException;

}