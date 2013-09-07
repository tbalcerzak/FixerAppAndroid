package pl.exorigoupos.fixerapp.services;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import pl.exorigoupos.fixerapp.data.Constants;
import pl.exorigoupos.fixerapp.json.HttpResponseAuthorizationJSONService;
import pl.exorigoupos.fixerapp.map.GPSTracker;
import pl.exorigoupos.fixerapp.sessions.SessionManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

public class AutoTrackingService extends Service {
	public static final long MINUTE_INTERVAL = 60 * 1000; // 1 min
	private SessionManager sManager;
	private Handler mHandler = new Handler();
	private Timer mTimer = null;
	private HashMap<String, String> userDetailsMap;
	private Bundle bundle;
	private GPSTracker gps;
	private Geocoder geocoder;
	private String latitude;
	private String longitude;
	private String city = "";
	private String street = "";
	private String number = "";
	private String working_range = "";
	private int intervalLoop = 0;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		
		sManager = new SessionManager(getApplicationContext());
		userDetailsMap = sManager.getUserDetails();
		working_range = userDetailsMap.get("working_range");
		geocoder = new Geocoder(this);

		switch (sManager.getAutoTrackingTime()) {
		case 0:
			intervalLoop = 10;
			break;
		case 1:
			intervalLoop = 30;
			break;
		case 2:
			intervalLoop = 60;
			break;

		default:
			break;
		}
		// cancel if already existed
		if (mTimer != null) {
			mTimer.cancel();
		} else {
			// recreate new
			mTimer = new Timer();
		}
		// schedule task
		mTimer.scheduleAtFixedRate(new AutoTrakingTask(), intervalLoop * MINUTE_INTERVAL, intervalLoop
				* MINUTE_INTERVAL);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mTimer.cancel();
		super.onDestroy();
	}

	public boolean isGooglePSA() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if (resultCode == ConnectionResult.SUCCESS) {
			return true;
		} else if (resultCode == ConnectionResult.SERVICE_MISSING
				|| resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED
				|| resultCode == ConnectionResult.SERVICE_DISABLED) {
			return false;
		}
		return false;
	}

	public boolean isNetworkConnected() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo == null) {
			return false;
		}
		return activeNetworkInfo.isConnected();
	}

	public void getGPSLocation() {

		if (isGooglePSA()) {

			gps = new GPSTracker(AutoTrackingService.this);
			if (gps.canGetLocation()) {

				double latitudeD = gps.getLatitude();
				double longitudeD = gps.getLongitude();

				latitude = Double.toString(latitudeD);
				longitude = Double.toString(longitudeD);

				try {

					List<Address> returnedaddresses = geocoder.getFromLocation(latitudeD, longitudeD, 1);

					if (returnedaddresses != null) {
						// city =
						// String.valueOf(returnedaddresses.get(0).getAddressLine(1));
						if (!String.valueOf(returnedaddresses.get(0).getLocality()).isEmpty()) {
							city = String.valueOf(returnedaddresses.get(0).getLocality());
						}
						// street =
						// String.valueOf(returnedaddresses.get(0).getAddressLine(0));
						if (!String.valueOf(returnedaddresses.get(0).getThoroughfare()).isEmpty()) {
							street = String.valueOf(returnedaddresses.get(0).getThoroughfare());
						}
						if (!String.valueOf(returnedaddresses.get(0).getFeatureName()).isEmpty()) {
							number = String.valueOf(returnedaddresses.get(0).getFeatureName());
						}

						// mAuthTask.execute((Void) null);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// can't get location
				// GPS or Network is not enabled
				// Ask user to enable GPS/network in settings
				gps.showSettingsAlert();
			}
		}
	}

	public class AutoTrakingTask extends TimerTask {

		@Override
		public void run() {
			// run on another thread
			boolean isConnected = isNetworkConnected();
			gps = new GPSTracker(AutoTrackingService.this);
			if (isConnected) {

				if (gps.canGetLocation()) {
					getGPSLocation();

					URL serverURL = null;

					try {
						serverURL = new URL(Constants.SERVER_URL);

					} catch (MalformedURLException e) {
						// handle exception...
					}

					// Create new JSON-RPC 2.0 client session
					JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
					mySession.setConnectionConfigurator(new MyConfigurator());
					// Construct new request
					String method = "saveAvailability";
					String servicemanId = userDetailsMap.get(SessionManager.KEY_ID);
					int requestID = 0;

					Map<String, Object> parameters = new HashMap<String, Object>();
					parameters.put("servicemanId", servicemanId);
					parameters.put("city", city);
					parameters.put("street", street);
					parameters.put("number", number);
					parameters.put("working_range", working_range);
					parameters.put("lat", latitude);
					parameters.put("lng", longitude);

					JSONRPC2Request request = new JSONRPC2Request(method, parameters, requestID);

					// Send request
					JSONRPC2Response response = null;

					try {
						response = mySession.send(request);

					} catch (JSONRPC2SessionException e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
						Log.e("AAA", e.getMessage(), e);
					}
					if (HttpResponseAuthorizationJSONService.authResponseFromJSON(response)) {
						// Log.e("json", response.getResult().toString());
						sManager.setUserCity(city);
						sManager.setUserNumber(number);
						sManager.setUserRange(working_range);
						sManager.setUserStreet(street);
						// mHandler.post(new Runnable() {
						//
						// @Override
						// public void run() {
						// // TODO Auto-generated method stub
						// Toast.makeText(
						// getApplicationContext(),
						// "Aktualny adress: " + city + " " + street + " " +
						// number
						// + " " + latitude + " "
						// + longitude, Toast.LENGTH_LONG).show();
						// }
						// });

						System.out.println(city + " " + street + " " + number + " " + latitude + " " + longitude);
						System.out.println(response.getResult());
					} else {
						System.out.println(response.getError().getMessage());
					}
				} else {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(getApplicationContext(),
									"FixerApp - odświeżanie lokalizacji: GPS jest wyłączony", Toast.LENGTH_LONG).show();
						}
					});
				}

			} else {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(),
								"FixerApp - odświeżanie lokalizacji: Brak połączenia z internetem", Toast.LENGTH_LONG)
								.show();
					}
				});
			}
		}

		public class MyConfigurator implements ConnectionConfigurator {

			public void configure(HttpURLConnection connection) {

				// add custom HTTP header
				connection.addRequestProperty("Token", userDetailsMap.get(SessionManager.KEY_TOKEN));
			}
		}

	}
}