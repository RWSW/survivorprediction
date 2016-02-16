package com.rwsw.fantasysurvivor.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

/**
 * This class is the base class of all activities of project. This class offers all
 * subclasses an easy access to a {@link com.octo.android.robospice.SpiceManager}
 * that is linked to the {@link android.app.Activity}
 * lifecycle.
 *
 * @author Jorge Cuesta
 */
public abstract class FragmentSpiceActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected void onStart() {
        if (!FantasySurvivor.getSpiceManager().isStarted()) {
            FantasySurvivor.getSpiceManager().start(this);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (FantasySurvivor.getSpiceManager().isStarted()) {
            FantasySurvivor.getSpiceManager().shouldStop();
        }
        super.onStop();
    }


}
