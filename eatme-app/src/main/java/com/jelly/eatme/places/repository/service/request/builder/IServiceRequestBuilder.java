package com.jelly.eatme.places.repository.service.request.builder;

import com.jelly.eatme.places.repository.service.ServiceException;
import com.jelly.eatme.places.repository.service.request.IServiceRequest;

public interface IServiceRequestBuilder {

    IServiceRequest build() throws ServiceException;

}