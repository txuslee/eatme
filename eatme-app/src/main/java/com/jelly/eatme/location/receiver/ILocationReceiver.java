package com.jelly.eatme.location.receiver;

import android.location.LocationListener;

public interface ILocationReceiver {

    void receive(LocationListener listener);

    void cancel();

}
