package com.jelly.eatme.location.requester.impl;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.LocationManager;
import com.jelly.eatme.application.EatmeConstants;
import com.jelly.eatme.location.requester.ILocationRequester;

public class LocationRequester implements ILocationRequester {

    protected final LocationManager manager;

    public LocationRequester(LocationManager manager) {
        this.manager = manager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestLocationUpdates(long minTime, long minDistance, Criteria criteria, PendingIntent pendingIntent) {
        // Gingerbread supports a location update request that accepts criteria directly.
        // Note that we aren't monitoring this provider to check if it becomes disabled - this is handled by the calling Activity.
        this.manager.requestLocationUpdates(minTime, minDistance, criteria, pendingIntent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestPassiveLocationUpdates(long minTime, long minDistance, PendingIntent pendingIntent) {
        // Froyo introduced the Passive Location Provider, which receives updates whenever a 3rd party app receives location updates.
        this.manager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, EatmeConstants.MAX_TIME, EatmeConstants.MAX_DISTANCE, pendingIntent);
    }

}
