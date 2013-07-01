package com.jelly.eatme.location.receiver.impl;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.jelly.eatme.location.receiver.ILocationReceiver;

public class SingleUpdateLocationReceiver extends BroadcastReceiver implements ILocationReceiver {

    private static final String SINGLE_LOCATION_UPDATE_ACTION = "com.jelly.eatme.event.SINGLE_LOCATION_UPDATE_ACTION";

    private final PendingIntent pendingIntent;
    private final LocationManager manager;
    private LocationListener listener;
    private final Criteria criteria;
    private final Context context;

    public SingleUpdateLocationReceiver(Context context) {
        this.manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // Construct the Pending Intent that will be broadcast by the oneshot location update.
        final Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);
        this.pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Coarse accuracy is specified here to get the fastest possible result.
        // The calling Activity will likely (or have already) request ongoing
        // updates using the Fine location provider.
        this.criteria = new Criteria();
        this.criteria.setAccuracy(Criteria.ACCURACY_LOW);
        this.context = context;
    }

    @Override
    public void receive(LocationListener listener) {
        IntentFilter intentFilter = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);
        this.context.registerReceiver(this, intentFilter);
        this.manager.requestSingleUpdate(criteria, this.pendingIntent);
        this.listener = listener;
    }

    /**
     * This {@link BroadcastReceiver} listens for a single location
     * update before unregistering itself.
     * The oneshot location update is returned via the {@link LocationListener}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        context.unregisterReceiver(this);

        final String key = LocationManager.KEY_LOCATION_CHANGED;
        final Location location = (Location) intent.getExtras().get(key);
        if (this.listener != null && location != null) {
            this.listener.onLocationChanged(location);
        }

        this.manager.removeUpdates(this.pendingIntent);
    }

    @Override
    public void cancel() {
        this.manager.removeUpdates(this.pendingIntent);
    }

}
