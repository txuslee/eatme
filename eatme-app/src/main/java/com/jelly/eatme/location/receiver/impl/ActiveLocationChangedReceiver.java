package com.jelly.eatme.location.receiver.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.jelly.eatme.application.EatmeConstants;
import com.jelly.eatme.location.receiver.ILocationReceiver;
import com.jelly.eatme.places.services.PlacesUpdateService;

/**
 * This Receiver class is used to listen for Broadcast Intents that announce
 * that a location change has occurred. This is used instead of a LocationListener
 * within an Activity is our only action is to start a service.
 */
public class ActiveLocationChangedReceiver extends BroadcastReceiver implements ILocationReceiver {

    private LocationListener listener;

    @Override
    public void receive(LocationListener listener) {
    }

    /**
     * When a new location is received, extract it from the Intent and use
     * it to start the Service used to update the list of nearby places.
     * <p/>
     * This is the Active receiver, used to receive Location updates when
     * the Activity is visible.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String providerEnabledKey = LocationManager.KEY_PROVIDER_ENABLED;
        if (intent.hasExtra(providerEnabledKey)) {
            if (!intent.getBooleanExtra(providerEnabledKey, true)) {
                //Intent providerDisabledIntent = new Intent(PlacesConstants.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED);
                //context.sendBroadcast(providerDisabledIntent);
            }
        }

        final String locationChangedKey = LocationManager.KEY_LOCATION_CHANGED;
        if (intent.hasExtra(locationChangedKey)) {
            final Location location = (Location) intent.getExtras().get(locationChangedKey);
            Intent updateServiceIntent = new Intent(context, PlacesUpdateService.class);
            updateServiceIntent.putExtra(EatmeConstants.EXTRA_KEY_LOCATION, location);
            updateServiceIntent.putExtra(EatmeConstants.EXTRA_KEY_RADIUS, EatmeConstants.DEFAULT_RADIUS);
            updateServiceIntent.putExtra(EatmeConstants.EXTRA_KEY_FORCEREFRESH, true);
            context.startService(updateServiceIntent);
        }
    }

    @Override
    public void cancel() {
    }

}
