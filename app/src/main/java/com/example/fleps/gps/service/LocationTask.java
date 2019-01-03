package com.example.fleps.gps.service;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.example.fleps.gps.CurrentLocation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.TreeMap;

public class LocationTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... strings) {
        Location location = CurrentLocation.getCurrentLocation();
        if ( location != null ) {
            HashMap<String, String> map = new HashMap<>();
            map.put("author", "2");
            map.put("fields[posiciones][00][acf_fc_layout]", "posiciones");
            map.put("fields[posiciones][00][direccion][lat]", CurrentLocation.getCurrentLocation().getLatitude() + "");
            map.put("fields[posiciones][00][direccion][lng]", CurrentLocation.getCurrentLocation().getLongitude() + "");

            Log.d("test", "result from server");

            // -------------------
            TreeMap<String, String> params = new TreeMap<String, String>(map);
            String url = "https://farbis.pe/agenda/wp-json/wp/v2/direcciones/34345";
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

                    Log.d("test", "result from server: " + result.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    conn.disconnect();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
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