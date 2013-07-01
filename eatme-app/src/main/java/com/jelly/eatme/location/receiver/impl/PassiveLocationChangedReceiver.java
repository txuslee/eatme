package com.jelly.eatme.location.receiver.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import com.jelly.eatme.application.EatmeConstants;
import com.jelly.eatme.location.provider.impl.LastLocationProvider;
import com.jelly.eatme.places.services.PlacesUpdateService;

/**
 * This Receiver class is used to listen for Broadcast Intents that announce
 * that a location change has occurred while this application isn't visible.
 * <p/>
 * Where possible, this is triggered by a Passive Location listener.
 */
public class PassiveLocationChangedReceiver extends BroadcastReceiver {

    /**
     * When a new location is received, extract it from the Intent and use
     * it to start the Service used to update the list of nearby places.
     * <p/>
     * This is the Passive receiver, used to receive Location updates from
     * third party apps when the Activity is not visible.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String key = LocationManager.KEY_LOCATION_CHANGED;
        Location location = null;

        if (intent.hasExtra(key)) {
            // This update came from Passive provider, so we can extract the location directly.
            location = (Location) intent.getExtras().get(key);
        } else {
            // This update came from a recurring alarm. We need to determine if there
            // has been a more recent Location received than the last location we used.

            // Get the best last location detected from the providers.
            LastLocationProvider lastLocationFinder = new LastLocationProvider(context, new SingleUpdateLocationReceiver(context));
            location = lastLocationFinder.findLocation(EatmeConstants.MAX_DISTANCE, System.currentTimeMillis() - EatmeConstants.MAX_TIME);
            SharedPreferences prefs = context.getSharedPreferences(EatmeConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);

            // Get the last location we used to get a listing.
            long lastTime = prefs.getLong(EatmeConstants.SP_KEY_LAST_LIST_UPDATE_TIME, Long.MIN_VALUE);
            long lastLat = prefs.getLong(EatmeConstants.SP_KEY_LAST_LIST_UPDATE_LAT, Long.MIN_VALUE);
            long lastLng = prefs.getLong(EatmeConstants.SP_KEY_LAST_LIST_UPDATE_LNG, Long.MIN_VALUE);
            Location lastLocation = new Location(EatmeConstants.CONSTRUCTED_LOCATION_PROVIDER);
            lastLocation.setLatitude(lastLat);
            lastLocation.setLongitude(lastLng);

            // Check if the last location detected from the providers is either too soon, or too close to the last
            // value we used. If it is within those thresholds we set the location to null to prevent the update
            // Service being run unnecessarily (and spending battery on data transfers).
            if ((lastTime > System.currentTimeMillis() - EatmeConstants.MAX_TIME) ||
                    (lastLocation.distanceTo(location) < EatmeConstants.MAX_DISTANCE))
                location = null;
        }

        // Start the Service used to find nearby points of interest based on the last detected location.
        if (location != null) {
            Intent updateServiceIntent = new Intent(context, PlacesUpdateService.class);
            updateServiceIntent.putExtra(EatmeConstants.EXTRA_KEY_LOCATION, location);
            updateServiceIntent.putExtra(EatmeConstants.EXTRA_KEY_RADIUS, EatmeConstants.DEFAULT_RADIUS);
            updateServiceIntent.putExtra(EatmeConstants.EXTRA_KEY_FORCEREFRESH, false);
            context.startService(updateServiceIntent);
        }
    }

}