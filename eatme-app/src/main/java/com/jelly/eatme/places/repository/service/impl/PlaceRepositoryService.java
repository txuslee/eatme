package com.jelly.eatme.places.repository.service.impl;

import com.jelly.eatme.places.domain.PlaceList;
import com.jelly.eatme.places.domain.key.IPlaceKey;
import com.jelly.eatme.places.domain.key.IPrimaryKey;
import com.jelly.eatme.places.repository.service.IPlaceRepositoryService;
import com.jelly.eatme.places.repository.service.ServiceException;
import com.jelly.eatme.places.repository.service.client.IRestServiceClient;
import com.jelly.eatme.places.repository.service.client.impl.RestServiceClient;
import com.jelly.eatme.places.repository.service.request.IServiceRequest;
import com.jelly.eatme.places.repository.service.request.builder.IPlaceServiceRequestBuilder;
import com.jelly.eatme.places.repository.service.request.builder.impl.PlaceServiceRequestBuilder;

public class PlaceRepositoryService implements IPlaceRepositoryService {

    private final IRestServiceClient client;

    public PlaceRepositoryService() {
        this(new RestServiceClient());
    }

    public PlaceRepositoryService(IRestServiceClient client) {
        this.client = client;
    }

    @Override
    public void initialize() {
        this.client.initialize();
    }

    @Override
    public <R> R invoke(IServiceRequest request, Class<R> type) throws ServiceException {
        return this.client.invoke(request, type);
    }

    @Override
    public PlaceList read(IPrimaryKey key) throws ServiceException {
        final IPlaceKey placeKey = (IPlaceKey) key;
        final IPlaceServiceRequestBuilder requestBuilder = new PlaceServiceRequestBuilder();
        requestBuilder.withLocation(placeKey.getLongitude(), placeKey.getLatitude())
                .withRadius(placeKey.getRadius())
                .withTypes(placeKey.getTypes())
                .withKey(placeKey.getKey());
        return this.invoke(requestBuilder.build(), PlaceList.class);
    }

    @Override
    public void shutdown() {
        this.client.shutdown();
    }

}
