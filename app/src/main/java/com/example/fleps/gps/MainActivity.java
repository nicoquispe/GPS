package com.example.fleps.gps;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.fleps.gps.listener.UpdateListener;
import com.example.fleps.gps.service.LocationService;

public class MainActivity extends Activity implements View.OnClickListener, UpdateListener {

    double latitude, longitude;
    TextView latTW, longTW, provTW;
    Button startBTN, stopBTN;
    private ProgressDialog dialog;
    public static final String log = "LOG: ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Config.context = this;
        startService(new Intent(this, LocationService.class));
        setCoordOnTW();
        CurrentLocation currentLocation = new CurrentLocation();
        currentLocation.addListener(this);

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void setCoordOnTW () {
        /*
        if (CurrentLocation.getCurrentLocation()!=null) {
            provTW.setText(getString(R.string.providerTW) + " " + CurrentLocation.getProvider());
            latTW.setText(getString(R.string.latitude) + " " + CurrentLocation.getCurrentLocation().getLatitude());
            longTW.setText(getString(R.string.longitude) + " " + CurrentLocation.getCurrentLocation().getLongitude());
        }
        */
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            /*
            case R.id.start_btn:
                startService(new Intent(this, LocationService.class));
                break;
            case R.id.stop_btn:
                stopService(new Intent(this, LocationService.class));
                break;
            */
        }
    }

    @Override
    public void onPositionChanged() {
        setCoordOnTW();
    }

    @Override
    public void onProviderChanged() {
        setCoordOnTW();
    }

}
