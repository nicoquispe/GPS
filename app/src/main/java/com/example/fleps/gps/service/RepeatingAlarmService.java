package com.example.fleps.gps.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.example.fleps.gps.CurrentLocation;

/**
 * Created by Fleps_000 on 07.07.2015.
 */
public class RepeatingAlarmService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = CurrentLocation.getCurrentLocation();
        if( location != null ) {
            new LocationTask().execute();
        }
    }
}
