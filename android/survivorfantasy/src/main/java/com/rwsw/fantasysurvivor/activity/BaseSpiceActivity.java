package com.rwsw.fantasysurvivor.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.rwsw.fantasysurvivor.activity.FantasySurvivor;

/**
 * This class is the base class of all activities of project. This class offers all
 * subclasses an easy access to a {@link com.octo.android.robospice.SpiceManager}
 * that is linked to the {@link android.app.Activity}
 * lifecycle.
 *
 * @author Jorge Cuesta
 */
public abstract class BaseSpiceActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
