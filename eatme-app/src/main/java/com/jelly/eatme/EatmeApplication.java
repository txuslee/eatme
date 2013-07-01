package com.jelly.eatme;

import android.app.Application;

public class EatmeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //if (EatmeConstants.DEVELOPER_MODE) {
        //    IStrictMode strictMode = PlatformSpecificImplementationFactory.getStrictMode();
        //    if (strictMode != null)
        //        strictMode.enableStrictMode();
        //}
    }

}
