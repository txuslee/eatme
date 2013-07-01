package com.jelly.eatme.location.receiver.impl;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import com.jelly.eatme.application.EatmeConstants;
import com.jelly.eatme.location.requester.impl.LocationRequester;

/**
 * This Receiver class is designed to listen for system boot.
 * <p/>
 * If the app has been run at least once, the passive location
 * updates should be enabled after a reboot.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(EatmeConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        boolean runOnce = prefs.getBoolean(EatmeConstants.SP_KEY_RUN_ONCE, false);

        if (runOnce) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Instantiate a Location Update Requester class based on the available platform version.
            // This will be used to request location updates.
            LocationRequester locationUpdateRequester = new LocationRequester(locationManager);

            // Check the Shared Preferences to see if we are updating location changes.
            boolean followLocationChanges = prefs.getBoolean(EatmeConstants.SP_KEY_FOLLOW_LOCATION_CHANGES, true);

            if (followLocationChanges) {
                // Passive location updates from 3rd party apps when the Activity isn't visible.
                Intent passiveIntent = new Intent(context, PassiveLocationChangedReceiver.class);
                PendingIntent locationListenerPassivePendingIntent = PendingIntent.getActivity(context, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                locationUpdateRequester.requestPassiveLocationUpdates(EatmeConstants.PASSIVE_MAX_TIME, EatmeConstants.PASSIVE_MAX_DISTANCE, locationListenerPassivePendingIntent);
            }
        }
    }
}
