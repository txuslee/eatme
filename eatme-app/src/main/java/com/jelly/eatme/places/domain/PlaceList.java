package com.jelly.eatme.places.domain;

import com.google.api.client.util.Key;

import java.io.Serializable;
import java.util.List;

public class PlaceList implements Serializable {

    @Key
    public String status;

    @Key
    public List<Place> results;

    public String getStatus() {
        return status;
    }

    public List<Place> getResults() {
        return results;
    }

}