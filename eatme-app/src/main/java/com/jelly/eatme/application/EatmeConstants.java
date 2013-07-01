package com.jelly.eatme.application;

import android.app.AlarmManager;

public final class EatmeConstants {

    // The default search radius when searching for places nearby.
    public static int DEFAULT_RADIUS = 1500; // 1 mile = 1609 meters
    // The maximum distance the user should travel between location updates.
    public static int MAX_DISTANCE = DEFAULT_RADIUS / 2;
    // The maximum time that should pass before the user gets a location update.
    public static long MAX_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    // You will generally want passive location updates to occur less frequently
    // than active updates. You need to balance location freshness with battery life.
    // The location update distance for passive updates.
    public static int PASSIVE_MAX_DISTANCE = MAX_DISTANCE;
    // The location update time for passive updates
    public static long PASSIVE_MAX_TIME = MAX_TIME;
    // Use the GPS (fine location provider) when the Activity is visible?
    public static boolean USE_GPS_WHEN_ACTIVITY_VISIBLE = true;
    //When the user exits via the back button, do you want to disable
    // passive background updates.
    public static boolean DISABLE_PASSIVE_LOCATION_WHEN_USER_EXIT = false;

    // Maximum latency before you force a cached detail page to be updated.
    public static long MAX_DETAILS_UPDATE_LATENCY = AlarmManager.INTERVAL_DAY;

    public static String SHARED_PREFERENCE_FILE = "SHARED_PREFERENCE_FILE";
    public static String SP_KEY_FOLLOW_LOCATION_CHANGES = "SP_KEY_FOLLOW_LOCATION_CHANGES";
    public static String SP_KEY_LAST_LIST_UPDATE_TIME = "SP_KEY_LAST_LIST_UPDATE_TIME";
    public static String SP_KEY_LAST_LIST_UPDATE_LAT = "SP_KEY_LAST_LIST_UPDATE_LAT";
    public static String SP_KEY_LAST_LIST_UPDATE_LNG = "SP_KEY_LAST_LIST_UPDATE_LNG";
    public static String SP_KEY_RUN_ONCE = "SP_KEY_RUN_ONCE";

    public static String EXTRA_KEY_REFERENCE = "reference";
    public static String EXTRA_KEY_ID = "id";
    public static String EXTRA_KEY_LOCATION = "location";
    public static String EXTRA_KEY_RADIUS = "radius";
    public static String EXTRA_KEY_TIME_STAMP = "time_stamp";
    public static String EXTRA_KEY_FORCEREFRESH = "force_refresh";
    public static String EXTRA_KEY_IN_BACKGROUND = "EXTRA_KEY_IN_BACKGROUND";

    public static String ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED = "com.jelly.eatme.places.active_location_update_provider_disabled";

    public static String CONSTRUCTED_LOCATION_PROVIDER = "CONSTRUCTED_LOCATION_PROVIDER";

}
