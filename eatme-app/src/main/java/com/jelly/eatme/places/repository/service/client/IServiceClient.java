package com.jelly.eatme.places.repository.service.client;

import com.jelly.eatme.places.repository.service.ServiceException;

public interface IServiceClient<P> {

    void initialize();

    <R> R invoke(P request, Class<R> type) throws ServiceException;

    void shutdown();

}