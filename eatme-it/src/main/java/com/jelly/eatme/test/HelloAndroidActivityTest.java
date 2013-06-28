package com.jelly.eatme.test;

import android.test.ActivityInstrumentationTestCase2;
import com.jelly.eatme.HelloAndroidActivity;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<HelloAndroidActivity> {

    public HelloAndroidActivityTest() {
        super(HelloAndroidActivity.class);
    }

    public void testActivity() {
        HelloAndroidActivity activity = getActivity();
        assertNotNull(activity);
    }
}

