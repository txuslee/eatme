package com.jelly.eatme.places.domain.key.impl;

import com.jelly.eatme.places.domain.key.IPlaceKey;

public class PlaceKey implements IPlaceKey {

    private double longitude;
    private double latitude;
    private double radius;
    private String types;
    private String key;

    public PlaceKey(String key, double longitude, double latitude, double radius) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.radius = radius;
        this.key = key;
    }

    @Override
    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public double getLatitude() {
        return this.latitude;
    }

    @Override
    public double getRadius() {
        return this.radius;
    }

    @Override
    public String getTypes() {
        return this.types;
    }

    @Override
    public void setTypes(String types) {
        this.types = types;
    }

    @Override
    public String getKey() {
        return this.key;
    }

}
