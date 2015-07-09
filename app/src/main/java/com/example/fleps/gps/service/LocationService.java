package com.example.fleps.gps.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.example.fleps.gps.Config;
import com.example.fleps.gps.CurrentLocation;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * Created by Fleps_000 on 07.07.2015.
 */
public class LocationService extends Service implements LocationListener {


    public static final int INTERVAL = 5000; // 2 sec
    public static final int FIRST_RUN = 5000; // 2 seconds
    int REQUEST_CODE = 11223344;
    private LocationManager locationManager;
    AlarmManager alarmManager;
    static int i = 0;
    InetAddress inetAddress;

    @Override
    public void onCreate() {
        super.onCreate();
        Config.context = this;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        initBestProvider();
        initLocation();
        locationManager.requestLocationUpdates(CurrentLocation.getProvider(), 1000, 1, this);
        startService();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alarmManager != null) {
            Intent intent = new Intent(this, RepeatingAlarmService.class);
            alarmManager.cancel(PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0));
            Toast.makeText(this, "Service Stopped!", Toast.LENGTH_LONG).show();
        }
    }

    private void startService() {
        /*Intent intent = new Intent(this, RepeatingAlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + FIRST_RUN,
                INTERVAL,
                pendingIntent);
        */
        Toast.makeText(this, "Service Started.", Toast.LENGTH_LONG).show();
    }

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
    public void onLocationChanged(Location location) {
        i++;
        CurrentLocation.setCurrentLocation(location);
        Toast.makeText(this, "Provider: "
                + CurrentLocation.getProvider()
                + "\nLat: "
                + CurrentLocation.getCurrentLocation().getLatitude()
                + "\nLong: "
                + CurrentLocation.getCurrentLocation().getLongitude()
                + "\nCount: "
                + i
                , Toast.LENGTH_LONG).show();
        new RequestTask().execute("grope.io");
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

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("lat", Double.toString(CurrentLocation.getCurrentLocation().getLatitude()));
                jsonObject.put("long", Double.toString(CurrentLocation.getCurrentLocation().getLongitude()));

                if(inetAddress==null){
                    try {
                        inetAddress = InetAddress.getByName(params[0]);
                    } catch (Exception e) {
                        System.out.println("Exp=" + e);
                    }
                }
                if(inetAddress!=null) {
                    DatagramSocket sock = new DatagramSocket();

                    byte [] buf = (jsonObject.toString()).getBytes();

                    DatagramPacket pack = new DatagramPacket(buf, jsonObject.toString().length(), inetAddress, 12345);

                    sock.send(pack);

                    sock.close();
                }
            } catch (Exception e) {
                System.out.println("Exp=" + e);
            }
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
