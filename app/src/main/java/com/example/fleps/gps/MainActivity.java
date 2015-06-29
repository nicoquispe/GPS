package com.example.fleps.gps;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity implements View.OnClickListener, LocationListener{

    double latitude, longitude;
    TextView latTW, longTW;
    Button sendBTN;
    private ProgressDialog dialog;
    public static final String log = "LOG: ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latTW = (TextView) findViewById(R.id.lat_tw);
        longTW = (TextView) findViewById(R.id.long_tw);
        sendBTN = (Button) findViewById(R.id.send_btn);
        sendBTN.setOnClickListener(this);

        Location location;
        location = getLocation();
        onLocationChanged(location);
    }

    private void setCoordOnTW (Location location) {
        if (location!=null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            latTW.setText(getString(R.string.latitude) + " " + latitude);
            longTW.setText(getString(R.string.longitude) + " " + longitude);
        }
    }

    private Location getLocation(){
        Location loc;
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = LocationManager.NETWORK_PROVIDER;
        lm.requestLocationUpdates(provider, 1000, 1, this);
        loc = lm.getLastKnownLocation(provider);
        return loc;
    }

    @Override
    public void onClick(View view) {
        new RequestTask().execute("http://grope.io/testapp/savecoord.php");
    }

    @Override
    public void onLocationChanged(Location location) {
        setCoordOnTW(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("lat", Double.toString(latitude));
                jsonObject.put("long", Double.toString(longitude));

                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost postMethod = new HttpPost(params[0]);
                StringEntity se = new StringEntity(jsonObject.toString());
                postMethod.setEntity(se);
                postMethod.setHeader("Accept", "application/json");
                postMethod.setHeader("Content-type", "application/json");
                ResponseHandler responseHandler = new BasicResponseHandler();
                httpclient.execute(postMethod, responseHandler);
            } catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Sending...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
            super.onPreExecute();
        }
    }
}
