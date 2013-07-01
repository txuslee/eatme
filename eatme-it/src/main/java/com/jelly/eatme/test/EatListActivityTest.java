package com.jelly.eatme.test;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import com.jelly.eatme.view.activity.impl.EatmeActivity;

public class EatListActivityTest extends ActivityInstrumentationTestCase2<EatmeActivity> {

    public EatListActivityTest() {
        super(EatmeActivity.class);
    }

    @SmallTest
    public void test_activity_startup() {
        EatmeActivity activity = getActivity();
        assertNotNull(activity);
    }

}

