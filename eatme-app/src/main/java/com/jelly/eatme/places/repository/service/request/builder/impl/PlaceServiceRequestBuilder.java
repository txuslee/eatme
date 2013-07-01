package com.jelly.eatme.places.repository.service.request.builder.impl;

import com.jelly.eatme.places.repository.service.ServiceException;
import com.jelly.eatme.places.repository.service.request.IServiceRequest;
import com.jelly.eatme.places.repository.service.request.builder.IPlaceServiceRequestBuilder;
import com.jelly.eatme.places.repository.service.request.impl.ServiceRequest;

public class PlaceServiceRequestBuilder implements IPlaceServiceRequestBuilder {

    public static final String GOOGLEAPIS_PLACE_SEARCH = "https://maps.googleapis.com/maps/api/place/search/json?key=${key}&location=${lat_loc},${lng_loc}&radius=${radius}&types=${types}&sensor=false";

    private double longitude;
    private double latitude;
    private double radius;
    private String types;
    private String key;

    @Override
    public IPlaceServiceRequestBuilder withLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        return this;
    }

    @Override
    public IPlaceServiceRequestBuilder withRadius(double radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public IPlaceServiceRequestBuilder withTypes(String types) {
        this.types = types;
        return this;
    }

    @Override
    public IPlaceServiceRequestBuilder withKey(String key) {
        this.key = key;
        return this;
    }

    @Override
    public IServiceRequest build() throws ServiceException {
        IServiceRequest request = new ServiceRequest(IServiceRequest.Method.GET, GOOGLEAPIS_PLACE_SEARCH);
        request.build(this.key, String.valueOf(this.latitude), String.valueOf(this.longitude), String.valueOf(this.radius), this.types);
        return request;
    }

}
