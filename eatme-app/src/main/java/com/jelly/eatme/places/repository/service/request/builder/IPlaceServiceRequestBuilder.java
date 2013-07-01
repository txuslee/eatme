package com.jelly.eatme.places.repository.service.request.builder;

public interface IPlaceServiceRequestBuilder extends IServiceRequestBuilder {

    IPlaceServiceRequestBuilder withLocation(double longitude, double latitude);

    IPlaceServiceRequestBuilder withRadius(double radius);

    IPlaceServiceRequestBuilder withTypes(String types);

    IPlaceServiceRequestBuilder withKey(String key);

}
