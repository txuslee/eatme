package com.jelly.eatme.places.repository.service;

import org.apache.http.HttpException;

public class ServiceException extends HttpException {

    public ServiceException() {
        super();
    }

    public ServiceException(HttpException ex) {
        super("", ex);
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

}