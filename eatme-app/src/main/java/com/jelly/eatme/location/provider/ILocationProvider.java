package com.jelly.eatme.location.provider;

import android.location.Location;
import android.location.LocationListener;

/**
 * Classes that implement this interface must provide methods to
 * find the "best" (most accurate and timely) previously detected
 * location using whatever providers are available.
 * <p/>
 * Where a timely / accurate previous location is not detected, classes
 * should return the last location and create a one-shot update to find
 * the current location. The one-shot update should be returned via the
 * Location Listener passed in through setChangedLocationListener.
 * <p/>
 * CREDITS: https://code.google.com/p/android-protips-location/
 */
public interface ILocationProvider {

    /**
     * Find the most accurate and timely previously detected location
     * using all the location providers. Where the last result is beyond
     * the acceptable maximum distance or latency create a one-shot update
     * of the current location to be returned using the {@link LocationListener}
     * passed in through {@link registerLocationChangedListener}
     *
     * @param minDistance Minimum distance before we require a location update.
     * @param minTime     Minimum time required between location updates.
     * @return The most accurate and / or timely previously detected location.
     */
    Location findLocation(int minDistance, long minTime);

    /**
     * Set the {@link LocationListener} that may receive a one-shot current location update.
     *
     * @param listener LocationListener
     */
    public void registerLocationChangedListener(LocationListener listener);

    /**
     * Cancel the one-shot current location update.
     */
    public void cancel();

}
