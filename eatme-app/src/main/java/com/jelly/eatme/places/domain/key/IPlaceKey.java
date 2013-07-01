package com.jelly.eatme.places.domain.key;

public interface IPlaceKey extends IPrimaryKey {

    double getLongitude();

    double getLatitude();

    double getRadius();

    String getTypes();

    void setTypes(String types);

}
