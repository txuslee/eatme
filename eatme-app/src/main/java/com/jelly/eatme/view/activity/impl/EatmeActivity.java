package com.jelly.eatme.view.activity.impl;

import android.app.PendingIntent;
import android.content.*;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import com.jelly.eatme.R;
import com.jelly.eatme.application.EatmeConstants;
import com.jelly.eatme.location.provider.ILocationProvider;
import com.jelly.eatme.location.provider.impl.LastLocationProvider;
import com.jelly.eatme.location.receiver.impl.ActiveLocationChangedReceiver;
import com.jelly.eatme.location.receiver.impl.PassiveLocationChangedReceiver;
import com.jelly.eatme.location.receiver.impl.SingleUpdateLocationReceiver;
import com.jelly.eatme.location.requester.ILocationRequester;
import com.jelly.eatme.location.requester.impl.LocationRequester;
import com.jelly.eatme.places.services.PlacesUpdateService;
import com.jelly.eatme.view.activity.IActivity;
import com.jelly.eatme.view.fragment.impl.PlaceListFragment;
import com.jelly.eatme.view.fragment.impl.PlaceMapFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EatmeActivity extends FragmentActivity implements IActivity {

    private static final Logger Log = LoggerFactory.getLogger(EatmeActivity.class.getSimpleName());

    protected SharedPreferences prefs;
    protected SharedPreferences.Editor prefsEditor;
    protected LocationManager locationManager;

    protected Criteria criteria;
    protected ILocationProvider lastLocationFinder;
    protected ILocationRequester locationUpdateRequester;
    protected PendingIntent locationListenerPendingIntent;
    protected PendingIntent locationListenerPassivePendingIntent;

    protected PlaceListFragment placeListFragment;
    protected PlaceMapFragment placeMapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a handle to the Fragments
        this.placeListFragment = (PlaceListFragment) this.getSupportFragmentManager().findFragmentById(R.id.list_fragment);
        this.placeMapFragment = (PlaceMapFragment) PlaceMapFragment.newInstance();

        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Get a reference to the Shared Preferences and a Shared Preference Editor.
        prefs = getSharedPreferences(EatmeConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        prefsEditor = prefs.edit();
        // Save that we've been run once.
        prefsEditor.putBoolean(EatmeConstants.SP_KEY_RUN_ONCE, true);
        prefsEditor.commit();

        // Specify the Criteria to use when requesting location updates while the application is Active
        criteria = new Criteria();
        if (EatmeConstants.USE_GPS_WHEN_ACTIVITY_VISIBLE) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
        } else {
            criteria.setPowerRequirement(Criteria.POWER_LOW);
        }

        // Setup the location update Pending Intents
        Intent activeIntent = new Intent(this, ActiveLocationChangedReceiver.class);
        this.locationListenerPendingIntent = PendingIntent.getBroadcast(this, 0, activeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent passiveIntent = new Intent(this, PassiveLocationChangedReceiver.class);
        this.locationListenerPassivePendingIntent = PendingIntent.getBroadcast(this, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Instantiate a LastLocationFinder class.
        // This will be used to find the last known location when the application starts.
        this.lastLocationFinder = new LastLocationProvider(this.getApplicationContext(), new SingleUpdateLocationReceiver(this.getApplicationContext()));
        this.lastLocationFinder.registerLocationChangedListener(oneShotLastLocationUpdateListener);

        // Instantiate a Location Update Requester class based on the available platform version.
        // This will be used to request location updates.
        this.locationUpdateRequester = new LocationRequester(this.locationManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Commit shared preference that says we're in the foreground.
        prefsEditor.putBoolean(EatmeConstants.EXTRA_KEY_IN_BACKGROUND, false);
        prefsEditor.commit();

        // Get the last known location (and optionally request location updates) and
        // update the place list.
        boolean followLocationChanges = prefs.getBoolean(EatmeConstants.SP_KEY_FOLLOW_LOCATION_CHANGES, true);
        getLocationAndUpdatePlaces(followLocationChanges);
    }

    @Override
    protected void onPause() {
        // Commit shared preference that says we're in the background.
        prefsEditor.putBoolean(EatmeConstants.EXTRA_KEY_IN_BACKGROUND, true);
        prefsEditor.commit();

        // Stop listening for location updates when the Activity is inactive.
        disableLocationUpdates();
        super.onPause();
    }

    /**
     * Updates (or displays) the restaurant location Fragment when a restaurant is selected
     * normally by clicking a place on the Place List.
     *
     * @param reference Place Reference
     * @param id        Place Identifier
     */
    public void selectPlace(String reference, String id) {
        // If the layout includes a single "main fragment container" then
        // we want to hide the List Fragment and display the Detail Fragment.
        // A back-button click should reverse this operation.
        // This is the phone-portrait mode.
        if (this.findViewById(R.id.main_fragment_container) != null) {
            Bundle args = new Bundle();
            args.putString(EatmeConstants.EXTRA_KEY_REFERENCE, reference);
            args.putString(EatmeConstants.EXTRA_KEY_ID, id);
            this.placeMapFragment.setArguments(args);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.addToBackStack(null);
            ft.hide(this.placeListFragment);
            ft.replace(R.id.main_fragment_container, this.placeMapFragment);
            ft.show(this.placeMapFragment);
            ft.commit();
        }
    }

    /**
     * Find the last known location (using a {@link LastLocationProvider}) and updates the
     * place list accordingly.
     *
     * @param updateWhenLocationChanges Request location updates
     */
    protected void getLocationAndUpdatePlaces(boolean updateWhenLocationChanges) {
        // This isn't directly affecting the UI, so put it on a worker thread.
        AsyncTask<Void, Void, Void> findLastLocationTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Find the last known location, specifying a required accuracy of within the min distance between updates
                // and a required latency of the minimum time required between updates.
                Location lastKnownLocation = lastLocationFinder.findLocation(EatmeConstants.MAX_DISTANCE,
                        System.currentTimeMillis() - EatmeConstants.MAX_TIME);

                // Update the place list based on the last known location within a defined radius.
                // Note that this is *not* a forced update. The Place List Service has settings to
                // determine how frequently the underlying web service should be pinged. This function
                // is called everytime the Activity becomes active, so we don't want to flood the server
                // unless the location has changed or a minimum latency or distance has been covered.
                // TODO Modify the search radius based on user settings?
                updatePlaces(lastKnownLocation, EatmeConstants.DEFAULT_RADIUS, false);
                return null;
            }
        };
        findLastLocationTask.execute();

        // If we have requested location updates, turn them on here.
        toggleUpdatesWhenLocationChanges(updateWhenLocationChanges);
    }

    /**
     * Choose if we should receive location updates.
     *
     * @param updateWhenLocationChanges Request location updates
     */
    protected void toggleUpdatesWhenLocationChanges(boolean updateWhenLocationChanges) {
        // Save the location update status in shared preferences
        prefsEditor.putBoolean(EatmeConstants.SP_KEY_FOLLOW_LOCATION_CHANGES, updateWhenLocationChanges);
        prefsEditor.commit();

        // Start or stop listening for location changes
        if (updateWhenLocationChanges) {
            requestLocationUpdates();
        } else {
            disableLocationUpdates();
        }
    }

    /**
     * Start listening for location updates.
     */
    protected void requestLocationUpdates() {
        // Normal updates while activity is visible.
        this.locationUpdateRequester.requestLocationUpdates(EatmeConstants.MAX_TIME, EatmeConstants.MAX_DISTANCE, criteria, locationListenerPendingIntent);

        // Passive location updates from 3rd party apps when the Activity isn't visible.
        this.locationUpdateRequester.requestPassiveLocationUpdates(EatmeConstants.PASSIVE_MAX_TIME, EatmeConstants.PASSIVE_MAX_DISTANCE, locationListenerPassivePendingIntent);

        // Register a receiver that listens for when the provider I'm using has been disabled.
        IntentFilter intentFilter = new IntentFilter(EatmeConstants.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED);
        registerReceiver(locProviderDisabledReceiver, intentFilter);

        // Register a receiver that listens for when a better provider than I'm using becomes available.
        String bestProvider = locationManager.getBestProvider(criteria, false);
        String bestAvailableProvider = locationManager.getBestProvider(criteria, true);
        if (bestProvider != null && !bestProvider.equals(bestAvailableProvider)) {
            locationManager.requestLocationUpdates(bestProvider, 0, 0, bestInactiveLocationProviderListener, getMainLooper());
        }
    }

    /**
     * Stop listening for location updates
     */
    protected void disableLocationUpdates() {
        unregisterReceiver(locProviderDisabledReceiver);
        locationManager.removeUpdates(locationListenerPendingIntent);
        locationManager.removeUpdates(bestInactiveLocationProviderListener);
        if (isFinishing()) {
            lastLocationFinder.cancel();
        }
        if (EatmeConstants.DISABLE_PASSIVE_LOCATION_WHEN_USER_EXIT && isFinishing()) {
            locationManager.removeUpdates(locationListenerPassivePendingIntent);
        }
    }

    /**
     * One-off location listener that receives updates from the {@link LastLocationProvider}.
     * This is triggered where the last known location is outside the bounds of our maximum
     * distance and latency.
     */
    protected LocationListener oneShotLastLocationUpdateListener = new LocationListener() {
        public void onLocationChanged(Location l) {
            updatePlaces(l, EatmeConstants.DEFAULT_RADIUS, true);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }
    };

    /**
     * If the best Location Provider (usually GPS) is not available when we request location
     * updates, this listener will be notified if / when it becomes available. It calls
     * requestLocationUpdates to re-register the location listeners using the better Location
     * Provider.
     */
    protected LocationListener bestInactiveLocationProviderListener = new LocationListener() {
        public void onLocationChanged(Location l) {
        }

        public void onProviderDisabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
            // Re-register the location listeners using the better Location Provider.
            requestLocationUpdates();
        }
    };

    /**
     * If the Location Provider we're using to receive location updates is disabled while the
     * app is running, this Receiver will be notified, allowing us to re-register our Location
     * Receivers using the best available Location Provider is still available.
     */
    protected BroadcastReceiver locProviderDisabledReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean providerDisabled = !intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
            // Re-register the location listeners using the best available Location Provider.
            if (providerDisabled) {
                requestLocationUpdates();
            }
        }
    };

    /**
     * Update the list of nearby places centered on the specified Location, within the specified radius.
     * This will start the {@link PlacesUpdateService} that will poll the underlying web service.
     *
     * @param location     Location
     * @param radius       Radius (meters)
     * @param forceRefresh Force Refresh
     */
    protected void updatePlaces(Location location, int radius, boolean forceRefresh) {
        if (location != null) {
            Log.debug("Starting update places service at location '{},{}'", location.getLongitude(), location.getLatitude());
            // Start the PlacesUpdateService. Note that we use an action rather than specifying the class directly.
            // That's because we have different variations of the Service for different platform versions.
            Intent updateServiceIntent = new Intent(this, PlacesUpdateService.class);
            updateServiceIntent.putExtra(EatmeConstants.EXTRA_KEY_LOCATION, location);
            updateServiceIntent.putExtra(EatmeConstants.EXTRA_KEY_RADIUS, radius);
            updateServiceIntent.putExtra(EatmeConstants.EXTRA_KEY_FORCEREFRESH, forceRefresh);
            startService(updateServiceIntent);
        } else {
            Log.debug("Updating place list for: No Previous Location Found");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // Request the last known location.
                final Location lastKnownLocation = lastLocationFinder.findLocation(EatmeConstants.MAX_DISTANCE,
                        System.currentTimeMillis() - EatmeConstants.MAX_TIME);
                // Force an update
                this.updatePlaces(lastKnownLocation, EatmeConstants.DEFAULT_RADIUS, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

