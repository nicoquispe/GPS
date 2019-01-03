package com.example.fleps.gps.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Fleps_000 on 07.07.2015.
 */
public class OnBootLoading extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent serviceLauncher = new Intent(context, LocationService.class);
            context.startService(serviceLauncher);
        }
    }
}
