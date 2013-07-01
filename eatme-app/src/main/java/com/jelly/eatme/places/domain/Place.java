package com.jelly.eatme.places.domain;

import com.google.api.client.util.Key;

import java.io.Serializable;
import java.util.List;

public class Place implements Serializable {

    @Key
    public String id;

    @Key
    public String name;

    @Key
    public String reference;

    @Key
    public String icon;

    @Key
    public String vicinity;

    @Key
    public Geometry geometry;

    @Key
    public String formatted_address;

    @Key
    public String formatted_phone_number;

    @Key
    private List<String> types;

    public static class Geometry implements Serializable {
        @Key
        public Location location;
    }

    public static class Location implements Serializable {
        @Key
        public double lat;

        @Key
        public double lng;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getReference() {
        return reference;
    }

    public String getIcon() {
        return icon;
    }

    public String getVicinity() {
        return vicinity;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public String getFormatted_phone_number() {
        return formatted_phone_number;
    }

    public List<String> getTypes() {
        return types;
    }

    public String getTypesAsString() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.types.size(); ++i) {
            builder.append(this.types.get(i));
            if (i < this.types.size() - 1) {
                builder.append('|');
            }
        }
        return builder.toString();
    }

    public String toString() {
        return String.format("%s - %s - %s", name, id, reference);
    }

}