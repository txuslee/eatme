package com.jelly.eatme.places.services;

import android.app.IntentService;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import com.jelly.eatme.application.EatmeConstants;
import com.jelly.eatme.content.PlacesContentProvider;
import com.jelly.eatme.location.receiver.impl.ActiveLocationChangedReceiver;
import com.jelly.eatme.location.receiver.impl.ConnectivityChangedReceiver;
import com.jelly.eatme.places.domain.Place;
import com.jelly.eatme.places.domain.PlaceList;
import com.jelly.eatme.places.domain.key.IPlaceKey;
import com.jelly.eatme.places.domain.key.impl.PlaceKey;
import com.jelly.eatme.places.repository.service.ServiceException;
import com.jelly.eatme.places.repository.service.impl.PlaceRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlacesUpdateService extends IntentService {

    private static final Logger Log = LoggerFactory.getLogger(PlacesUpdateService.class.getSimpleName());
    private static final String PLACES_API_KEY = "";

    protected final PlaceRepositoryService repository;
    protected ConnectivityManager connectivityManager;
    protected ContentResolver contentResolver;
    protected SharedPreferences preferences;
    protected SharedPreferences.Editor preferencesEditor;
    protected boolean lowBattery = false;
    protected boolean mobileData = false;
    protected int prefetchCount = 0;

    public PlacesUpdateService() {
        super(PlacesUpdateService.class.getSimpleName());
        this.repository = new PlaceRepositoryService();
        this.repository.initialize();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        this.preferences = getSharedPreferences(EatmeConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        this.preferencesEditor = this.preferences.edit();
        this.contentResolver = getContentResolver();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Check if we're running in the foreground, if not, check if we have permission to do background updates.
        boolean backgroundAllowed = this.connectivityManager.getBackgroundDataSetting();
        boolean inBackground = this.preferences.getBoolean(EatmeConstants.EXTRA_KEY_IN_BACKGROUND, true);
        if (!backgroundAllowed && inBackground) return;

        // Extract the location and radius around which to conduct our search.
        Location location = new Location(EatmeConstants.CONSTRUCTED_LOCATION_PROVIDER);
        int radius = EatmeConstants.DEFAULT_RADIUS;

        Bundle extras = intent.getExtras();
        if (intent.hasExtra(EatmeConstants.EXTRA_KEY_LOCATION)) {
            location = (Location) (extras.get(EatmeConstants.EXTRA_KEY_LOCATION));
            radius = extras.getInt(EatmeConstants.EXTRA_KEY_RADIUS, EatmeConstants.DEFAULT_RADIUS);
        }

        // Check if we're in a low battery situation.
        IntentFilter batIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battery = registerReceiver(null, batIntentFilter);
        lowBattery = getIsLowBattery(battery);

        // Check if we're connected to a data network, and if so - if it's a mobile network.
        NetworkInfo activeNetwork = this.connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        mobileData = activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

        // If we're not connected, enable the connectivity receiver and disable the location receiver.
        // There's no point trying to poll the server for updates if we're not connected, and the
        // connectivity receiver will turn the location-based updates back on once we have a connection.
        if (!isConnected) {
            PackageManager pm = getPackageManager();
            ComponentName connectivityReceiver = new ComponentName(this, ConnectivityChangedReceiver.class);
            ComponentName locationReceiver = new ComponentName(this, ActiveLocationChangedReceiver.class);

            pm.setComponentEnabledSetting(connectivityReceiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            pm.setComponentEnabledSetting(locationReceiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        } else {
            // If we are connected check to see if this is a forced update (typically triggered
            // when the location has changed).
            boolean doUpdate = intent.getBooleanExtra(EatmeConstants.EXTRA_KEY_FORCEREFRESH, false);

            // If it's not a forced update (for example from the Activity being restarted) then
            // check to see if we've moved far enough, or there's been a long enough delay since
            // the last update and if so, enforce a new update.
            if (!doUpdate) {
                // Retrieve the last update time and place.
                long lastTime = this.preferences.getLong(EatmeConstants.SP_KEY_LAST_LIST_UPDATE_TIME, Long.MIN_VALUE);
                long lastLat = this.preferences.getLong(EatmeConstants.SP_KEY_LAST_LIST_UPDATE_LAT, Long.MIN_VALUE);
                long lastLng = this.preferences.getLong(EatmeConstants.SP_KEY_LAST_LIST_UPDATE_LNG, Long.MIN_VALUE);
                Location lastLocation = new Location(EatmeConstants.CONSTRUCTED_LOCATION_PROVIDER);
                lastLocation.setLatitude(lastLat);
                lastLocation.setLongitude(lastLng);

                // If update time and distance bounds have been passed, do an update.
                if ((lastTime < System.currentTimeMillis() - EatmeConstants.MAX_TIME) ||
                        (lastLocation.distanceTo(location) > EatmeConstants.MAX_DISTANCE)) {
                    doUpdate = true;
                }
            }

            if (doUpdate) {
                // Refresh the prefetch count for each new location.
                prefetchCount = 0;
                // Hit the server for new venues for the current location.
                refreshPlaces(location, radius);
            } else {
                Log.debug("Place List is fresh: Not refreshing");
            }
        }
        Log.debug("Place List Download Service Complete");
    }

    @Override
    public void setIntentRedelivery(boolean enabled) {
        super.setIntentRedelivery(true);
    }

    /**
     * Polls the underlying service to return a list of places within the specified
     * radius of the specified Location.
     *
     * @param location Location
     * @param radius   Radius
     */
    protected void refreshPlaces(Location location, int radius) {
        // Log to see if we'll be prefetching the details page of each new place.
        if (mobileData) {
            Log.debug("Not prefetching due to being on mobile");
        } else if (lowBattery) {
            Log.debug("Not prefetching due to low battery");
        }

        IPlaceKey key = new PlaceKey(PLACES_API_KEY, location.getLongitude(), location.getLatitude(), radius);
        key.setTypes("restaurant");
        try {
            long currentTime = System.currentTimeMillis();
            PlaceList places = this.repository.read(key);
            for (Place place : places.getResults()) {
                // Add each new place to the Places Content Provider
                this.addPlace(location, place, currentTime);
            }
            // Save the last update time and place to the Shared Preferences.
            this.preferencesEditor.putLong(EatmeConstants.SP_KEY_LAST_LIST_UPDATE_LAT, (long) location.getLatitude());
            this.preferencesEditor.putLong(EatmeConstants.SP_KEY_LAST_LIST_UPDATE_LNG, (long) location.getLongitude());
            this.preferencesEditor.putLong(EatmeConstants.SP_KEY_LAST_LIST_UPDATE_TIME, System.currentTimeMillis());
            this.preferencesEditor.commit();
        } catch (ServiceException e) {
            Log.error("", e);
        }
    }

    private boolean addPlace(Location location, Place place, long currentTime) {
        // Contruct the Content Values
        final ContentValues values = new ContentValues();
        final Place.Location geometry = place.getGeometry().location;
        values.put(PlacesContentProvider.KEY_ID, place.getId());
        values.put(PlacesContentProvider.KEY_NAME, place.getName());
        values.put(PlacesContentProvider.KEY_LOCATION_LAT, geometry.lat);
        values.put(PlacesContentProvider.KEY_LOCATION_LNG, geometry.lng);
        values.put(PlacesContentProvider.KEY_VICINITY, place.getVicinity());
        values.put(PlacesContentProvider.KEY_TYPES, place.getTypesAsString());
        values.put(PlacesContentProvider.KEY_ICON, place.getIcon());
        values.put(PlacesContentProvider.KEY_REFERENCE, place.getReference());
        values.put(PlacesContentProvider.KEY_LAST_UPDATE_TIME, currentTime);

        // Calculate the distance between the current location and the venue's location
        float distance = 0.0f;
        if (location != null && geometry != null) {
            final Location placeLocation = new Location(EatmeConstants.CONSTRUCTED_LOCATION_PROVIDER);
            placeLocation.setLatitude(geometry.lat);
            placeLocation.setLongitude(geometry.lng);
            distance = location.distanceTo(placeLocation);
        }
        values.put(PlacesContentProvider.KEY_DISTANCE, distance);

        // Update or add the new place to the PlacesContentProvider
        final String where = String.format("%s = '%s'", PlacesContentProvider.KEY_ID, place.getId());
        boolean result = false;
        try {
            if (contentResolver.update(PlacesContentProvider.CONTENT_URI, values, where, null) == 0) {
                if (contentResolver.insert(PlacesContentProvider.CONTENT_URI, values) != null) {
                    result = true;
                }
            } else {
                result = true;
            }
        } catch (Exception ex) {
            Log.error("Adding '{}' failed.", place.getName());
        }

        return result;
    }

    /**
     * Returns battery status. True if less than 10% remaining.
     *
     * @param battery Battery Intent
     * @return Battery is low
     */
    protected boolean getIsLowBattery(Intent battery) {
        float pctLevel = (float) battery.getIntExtra(BatteryManager.EXTRA_LEVEL, 1) / battery.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
        return pctLevel < 0.15;
    }

}
