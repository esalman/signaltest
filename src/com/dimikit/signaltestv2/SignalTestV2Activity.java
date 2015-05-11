package com.dimikit.signaltestv2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SignalTestV2Activity extends Activity {
	private Handler mHandler = new Handler();
	private TelephonyManager signalManager;
	private TelephonyManager telephonyManager;
	private GsmCellLocation cellLocation;
	private List<NeighboringCellInfo> neighboringList;
	private String GsmSignalStrength;
	private String GsmBitErrorRate;
	private List<NameValuePair> nameValuePairs;
	private HttpClient httpclient;
	private HttpPost httppost;
	private String lat;
	private String lon;
	private JSONObject jsonObject;
	private Calendar cal;
	private TextView tv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		tv = (TextView) findViewById(R.id.textView1);
		tv.setText("Hello DriveTest!");

		httpclient = new DefaultHttpClient();
		httppost = new HttpPost("http://demo.dimikit.com/handler.php");

		// SIGNAL STRENGTH
		signalManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		signalManager.listen(new PhoneStateListener() {
			@Override
			public void onSignalStrengthsChanged(final SignalStrength signal) {
				GsmSignalStrength = String.valueOf(String.valueOf(-113 + 2
						* signal.getGsmSignalStrength()));
				GsmBitErrorRate = String.valueOf(signal.getGsmBitErrorRate());
			}
		}, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		// LOCATION
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				lat = String.valueOf(location.getLatitude());
				lon = String.valueOf(location.getLongitude());
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

		// TIMER
		final Runnable mUpdateTimeTask = new Runnable() {
			public void run() {
				tv.setText("Uploading data...");
				// SIGNAL STRENGTH AND LOCATION
				nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("GsmSignalStrength",
						GsmSignalStrength));
				nameValuePairs.add(new BasicNameValuePair("GsmBitErrorRate",
						GsmBitErrorRate));
				nameValuePairs.add(new BasicNameValuePair("Latitude", lat));
				nameValuePairs.add(new BasicNameValuePair("Longitude", lon));

				telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				cellLocation = (GsmCellLocation) telephonyManager
						.getCellLocation();
				
				// String temp = String.valueOf(telephonyManager.getDeviceId());
				nameValuePairs.add(new BasicNameValuePair("Cid", String
						.valueOf(cellLocation.getCid())));
				nameValuePairs.add(new BasicNameValuePair("Lac", String
						.valueOf(cellLocation.getLac())));
				nameValuePairs.add(new BasicNameValuePair("DataState", String
						.valueOf(telephonyManager.getDataState())));
				nameValuePairs.add(new BasicNameValuePair("NetworkType", String
						.valueOf(telephonyManager.getNetworkType())));
				nameValuePairs.add(new BasicNameValuePair("PhoneType", String
						.valueOf(telephonyManager.getPhoneType())));
				nameValuePairs.add(new BasicNameValuePair("SimState", String
						.valueOf(telephonyManager.getSimState())));
				nameValuePairs.add(new BasicNameValuePair("DeviceId", String
						.valueOf(telephonyManager.getDeviceId())));
				nameValuePairs.add(new BasicNameValuePair(
						"DeviceSoftwareVersion", String
								.valueOf(telephonyManager
										.getDeviceSoftwareVersion())));
				nameValuePairs.add(new BasicNameValuePair("Line1Number", String
						.valueOf(telephonyManager.getLine1Number())));
				nameValuePairs
						.add(new BasicNameValuePair("NetworkCountryIso", String
								.valueOf(telephonyManager
										.getNetworkCountryIso())));
				nameValuePairs.add(new BasicNameValuePair("NetworkOperator",
						String.valueOf(telephonyManager.getNetworkOperator())));
				nameValuePairs.add(new BasicNameValuePair(
						"NetworkOperatorName", String.valueOf(telephonyManager
								.getNetworkOperatorName())));
				nameValuePairs.add(new BasicNameValuePair("SimCountryIso",
						String.valueOf(telephonyManager.getSimCountryIso())));
				nameValuePairs.add(new BasicNameValuePair("SimOperator", String
						.valueOf(telephonyManager.getSimOperator())));
				nameValuePairs.add(new BasicNameValuePair("SimOperatorName",
						String.valueOf(telephonyManager.getSimOperatorName())));
				nameValuePairs.add(new BasicNameValuePair("SimSerialNumber",
						String.valueOf(telephonyManager.getSimSerialNumber())));
				nameValuePairs.add(new BasicNameValuePair("SubscriberId",
						String.valueOf(telephonyManager.getSubscriberId())));

				neighboringList = telephonyManager.getNeighboringCellInfo();
				jsonObject = new JSONObject();
				for (int i = 0; i < neighboringList.size(); i++) {
					try {
						jsonObject.put(
								String.valueOf(i),
								String.valueOf(neighboringList.get(i).getCid())
										+ "|"
										+ String.valueOf(neighboringList.get(i)
												.getLac())
										+ "|"
										+ (-113 + 2 * neighboringList.get(i)
												.getRssi()));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				nameValuePairs.add(new BasicNameValuePair("Neighboring", String
						.valueOf(jsonObject)));

				try {
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
//					Toast.makeText(getApplicationContext(),
//							"Response: " + String.valueOf(response),
//							Toast.LENGTH_SHORT).show();
					cal = Calendar.getInstance();
					tv.setText(cal.getTime()
							+ " :: Data successfully uploaded on server." + "\nResponse: " + response);
				} catch (IOException e) {
					e.printStackTrace();
				}

				mHandler.postDelayed(this, 1000);

			}
		};

		// TOGGLE BUTTON
		final ToggleButton togglebutton = (ToggleButton) findViewById(R.id.toggleButton1);
		togglebutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/*Intent i=new Intent(android.content.Intent.ACTION_DIAL, Uri.parse("tel:*#*#4636#*#*"));
				startActivity(i);*/
				Intent in = new Intent(Intent.ACTION_MAIN);
			    in.setClassName("com.android.settings", "com.android.settings.TestingSettings");
			    startActivity(in);
				/*if (togglebutton.isChecked()) {
					mHandler.postDelayed(mUpdateTimeTask, 100);
				} else {
					mHandler.removeCallbacks(mUpdateTimeTask);
				}*/
			}
		});

	}

}