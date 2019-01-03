package com.example.fleps.gps.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.fleps.gps.MainActivity;
import com.example.fleps.gps.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
	private static final String TAG = "MyFirebaseMsgService";

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		super.onMessageReceived(remoteMessage);
		Log.d(TAG, "Message data payload: " + remoteMessage.getData());
		//sendNotification("aa", "asadsas");
		try {
			JSONObject json = new JSONObject(remoteMessage.getData().toString());
			JSONObject data = json.getJSONObject("data");
			String action = data.getString("action");
			switch ( action ) {
				case "tiempo_real":
					startTrackerService();
					break;
				case "detener_tiempo_real":
					sttopTrackerService();
					break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("WrongThread")
	@Override
	public void onNewToken(String token) {
		super.onNewToken(token);
		new LoginTask().execute(token);
	}


	private void sendNotification(String titleMessage, String messageBody) {
		Intent intent;
		intent = new Intent(this, MainActivity.class);

		intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 123456789, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		String channelId = getString(R.string.default_notification_channel_id);
		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle(titleMessage)
				.setContentText(messageBody)
				.setAutoCancel(true)
				//.setVibrate(new long[] { 1000, 1000})
				.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
				.setContentIntent(pendingIntent);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(channelId,
					"Channel human readable title",
					NotificationManager.IMPORTANCE_HIGH);
			assert notificationManager != null;
			notificationManager.createNotificationChannel(channel);
		}

		assert notificationManager != null;
		notificationManager.notify(  123456789, notificationBuilder.build());
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

			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);

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

	private void startTrackerService() {
		Intent serviceLauncher = new Intent(getApplicationContext(), LocationService.class);
		stopService(serviceLauncher);
		startService(serviceLauncher);
	}
	private void sttopTrackerService() {
		Intent serviceLauncher = new Intent(getApplicationContext(), LocationService.class);
		stopService(serviceLauncher);
	}
}
