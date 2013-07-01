package com.jelly.eatme.location.provider.impl;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.jelly.eatme.location.provider.ILocationProvider;
import com.jelly.eatme.location.receiver.ILocationReceiver;

import java.util.List;

/*
* Optimized implementation of Last Location Finder for devices running Gingerbread
* and above.
*
* This class let's you find the "best" (most accurate and timely) previously
* detected location using whatever providers are available.
*
* Where a timely / accurate previous location is not detected it will
* return the newest location (where one exists) and setup a oneshot
* location update to find the current location.
*
* CREDITS: https://code.google.com/p/android-protips-location/
* */
public class LastLocationProvider implements ILocationProvider {

    private final LocationManager manager;
    private final ILocationReceiver receiver;
    private LocationListener listener;
    private final Context context;

    public LastLocationProvider(Context context, ILocationReceiver receiver) {
        this.manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.receiver = receiver;
        this.context = context;
    }

    /**
     * Returns the most accurate and timely previously detected location.
     * Where the last result is beyond the specified maximum distance or
     * latency a one-off location update is returned via the {@link LocationListener}
     * specified in {@link registerLocationChangedListener}.
     *
     * @param minDistance Minimum distance before we require a location update.
     * @param minTime     Minimum time required between location updates.
     * @return The most accurate and / or timely previously detected location.
     */
    @Override
    public Location findLocation(int minDistance, long minTime) {
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;
        Location bestResult = null;

        // Iterate through all the providers on the system, keeping
        // note of the most accurate result within the acceptable time limit.
        // If no result is found within maxTime, return the newest Location.
        List<String> matchingProviders = this.manager.getAllProviders();
        for (String provider : matchingProviders) {
            final Location location = this.manager.getLastKnownLocation(provider);
            if (location != null) {
                final float accuracy = location.getAccuracy();
                final long time = location.getTime();
                if ((time > minTime && accuracy < bestAccuracy)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }

        // If the best result is beyond the allowed time limit, or the accuracy of the
        // best result is wider than the acceptable maximum distance, request a single update.
        // This check simply implements the same conditions we set when requesting regular
        // location updates every [minTime] and [minDistance].
        if (this.listener != null && (bestTime < minTime || bestAccuracy > minDistance)) {
            this.receiver.receive(this.listener);
        }
        return bestResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerLocationChangedListener(LocationListener listener) {
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel() {
        this.receiver.cancel();
    }

}
