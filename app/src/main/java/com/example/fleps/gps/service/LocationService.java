package com.example.fleps.gps.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.example.fleps.gps.Config;
import com.example.fleps.gps.CurrentLocation;
import com.example.fleps.gps.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by Fleps_000 on 07.07.2015.
 */
public class LocationService extends Service implements LocationListener {

    public static final int INTERVAL = 1000; // 2 sec
    public static final int FIRST_RUN = 1000; // 2 seconds
    int REQUEST_CODE = 11223344;
    private LocationManager locationManager;
    AlarmManager alarmManager;
    static int i = 0;
    InetAddress inetAddress;
    private Task<AuthResult> mytask = null;
    private FusedLocationProviderClient mFusedLocationClient;


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        String currentCorreo = "admin@farbis.pe";
        String currentPassword = "Ricardo1*";
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        /*
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                currentCorreo, currentPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mytask = task;
                } else {
                    Log.d(TAG, "Firebase authentication failed");
                }
            }
        });
        */

        registrar_uuid();
        if ( Config.context != null ) {
            if ( Config.context instanceof Activity ) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener((Activity) Config.context, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    CurrentLocation.setCurrentLocation(location);
                                    new LocationTask().execute();
                                }
                            }
                        });
            }
        }

        Config.context = this;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        initBestProvider();
        initLocation();
        locationManager.requestLocationUpdates(CurrentLocation.getProvider(), 1000, 1, this);
        startService();

    }

    private void registrar_uuid() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH));
        }

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult().getToken();

						ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("label", token);
						clipboard.setPrimaryClip(clip);
                        new LoginTask().execute(token);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alarmManager != null) {
            Intent intent = new Intent(this, RepeatingAlarmService.class);
            alarmManager.cancel(PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0));
        }
    }

    @SuppressLint("MissingPermission")
    private void startService() {
        Intent intent = new Intent(this, RepeatingAlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + FIRST_RUN,
                INTERVAL,
                pendingIntent);


    }

    @SuppressLint("MissingPermission")
    private void initLocation() {
        CurrentLocation.setCurrentLocation(locationManager.getLastKnownLocation(CurrentLocation.getProvider()));
    }

    private void initBestProvider() {
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        CurrentLocation.setProvider(locationManager.getBestProvider(criteria, true));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(final Location location) {
        i++;
        CurrentLocation.setCurrentLocation(location);
        new LocationTask().execute();
        /*
        if( mytask != null ) {
            String uuid = mytask.getResult().getUser().getUid();

            final String path = "locations";
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path+"/"+uuid);
            Log.d(TAG, "Firebase isSuccessful");
            if (location != null) {
                HashMap<String, Object> result = new HashMap<>();
                result.put("lat", CurrentLocation.getCurrentLocation().getLatitude());
                result.put("lng", CurrentLocation.getCurrentLocation().getLongitude());
                result.put("sender",uuid);
                ref.setValue(result);
                Log.d(TAG, "Firebase location");
            }
        }
        */
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void sendPost( TreeMap<String, String> params, String url ) {
        try{
            StringBuilder sbParams = new StringBuilder();
            int i = 0;
            for (String key : params.keySet()) {
                try {
                    if (i != 0){
                        sbParams.append("&");
                    }
                    sbParams.append(key).append("=")
                            .append(URLEncoder.encode(params.get(key), "UTF-8"));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                i++;
            }

            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept-Charset", "UTF-8");

            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);

            conn.connect();

            String paramsString = sbParams.toString();

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(paramsString);
            wr.flush();
            wr.close();

            try {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                //Log.d("test", "result from server: " + result.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    class LoginTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put("os", "Android");
            map.put("email", "admin@farbis.pe");
            map.put("token", params[0] );
            map.put("lang", "es");
            sendPost(new TreeMap<String, String>(map), "https://farbis.pe/agenda/pnfw/register" );
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
