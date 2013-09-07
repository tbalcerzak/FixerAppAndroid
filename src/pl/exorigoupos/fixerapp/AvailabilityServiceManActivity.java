package pl.exorigoupos.fixerapp;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.exorigoupos.fixerapp.data.Constants;
import pl.exorigoupos.fixerapp.json.HttpResponseAuthorizationJSONService;
import pl.exorigoupos.fixerapp.map.GPSTracker;
import pl.exorigoupos.fixerapp.services.AutoTrackingService;
import pl.exorigoupos.fixerapp.sessions.SessionManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

public class AvailabilityServiceManActivity extends Activity {

	private String latitude;
	private String longitude;
	private String address = "";
	private String city = "";
	private String street = "";
	private String number = "";
	private String working_range = "";
	private EditText myCity;
	private EditText myStreet;
	private EditText myNumber;
	private EditText myWorking_range;
	private Button myAcceptBtn;
	private Button myGetGPSLocationBtn;
	private Button myMapViewLocationBtn;
	private CheckBox autoTrackingCheck;
	private Spinner spinnerTimeTracking;

	final int maxResult = 1;
	private ProgressDialog dialog;
	private HashMap<String, String> userDetailsMap;
	private SessionManager sManager;
	private GPSTracker gps;
	private SaveAvailabilityTask mAuthTask = null;
	private static Handler handler;

	private TextView myReturnedAddress, myReturnedLatitude, myReturnedLongitude;

	private Geocoder geocoder;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_availability_serviceman);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case 1:
					sManager.setUserCity(city);
					sManager.setUserNumber(number);
					sManager.setUserRange(working_range);
					sManager.setUserStreet(street);
					Intent service = new Intent(AvailabilityServiceManActivity.this, AutoTrackingService.class);
					if (autoTrackingCheck.isChecked()) {
						sManager.setAutoTracking(autoTrackingCheck.isChecked());
						sManager.setAutoTrackingTime(spinnerTimeTracking.getSelectedItemPosition());
						startService(service);
					} else {
						sManager.setAutoTracking(autoTrackingCheck.isChecked());
						sManager.setAutoTrackingTime(0);
						stopService(service);
					}
					finish();
					break;
				case 2:
					break;
				}
			}
		};
		sManager = new SessionManager(getApplicationContext());
		userDetailsMap = sManager.getUserDetails();

		autoTrackingCheck = (CheckBox) findViewById(R.id.checkBoxAutoTracking);
		spinnerTimeTracking = (Spinner) findViewById(R.id.spinnerTimeTracking);
		spinnerTimeTracking.setSelection(sManager.getAutoTrackingTime());
		myAcceptBtn = (Button) findViewById(R.id.btnAccept);
		myCity = (EditText) findViewById(R.id.city);
		myCity.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				myCity.setFocusable(true);
				myCity.setFocusableInTouchMode(true);
				return false;
			}
		});
		myStreet = (EditText) findViewById(R.id.street);
		myStreet.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				myStreet.setFocusable(true);
				myStreet.setFocusableInTouchMode(true);
				return false;
			}
		});
		myNumber = (EditText) findViewById(R.id.number);
		myNumber.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				myNumber.setFocusable(true);
				myNumber.setFocusableInTouchMode(true);
				return false;
			}
		});
		myWorking_range = (EditText) findViewById(R.id.working_range);
		myWorking_range.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				myWorking_range.setFocusable(true);
				myWorking_range.setFocusableInTouchMode(true);
				return false;
			}
		});
		myGetGPSLocationBtn = (Button) findViewById(R.id.btnGetGPSLocation);
		myMapViewLocationBtn = (Button) findViewById(R.id.btnMapViewLocation);

		myCity.setText(userDetailsMap.get(SessionManager.KEY_CITY));
		myStreet.setText(userDetailsMap.get(SessionManager.KEY_STREET));
		myNumber.setText(userDetailsMap.get(SessionManager.KEY_NUMBER));
		myWorking_range.setText(userDetailsMap.get(SessionManager.KEY_WORKING_RANGE));

		autoTrackingCheck.setChecked(sManager.isAutoTracking());

		TextView txCity = (TextView) findViewById(R.id.txtMiasto);
		txCity.requestFocus();
		geocoder = new Geocoder(this);
		mAuthTask = new SaveAvailabilityTask();

		myAcceptBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(AvailabilityServiceManActivity.this).setMessage("Jesteś pewien?")
						.setPositiveButton(android.R.string.ok, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialogI, int which) {
								// TODO Auto-generated method stub
								city = myCity.getText().toString();
								street = myStreet.getText().toString();
								number = myNumber.getText().toString();
								address = city + ", " + street + ", " + number;
								working_range = myWorking_range.getText().toString();

								if (isGooglePSA()) {
									boolean isConnected = isNetworkConnected();
									if (isConnected) {
										try {
											List<Address> returnedaddresses = geocoder.getFromLocationName(address,
													maxResult);
											if (returnedaddresses != null) {
												latitude = String.valueOf(returnedaddresses.get(0).getLatitude());
												longitude = String.valueOf(returnedaddresses.get(0).getLongitude());
												dialog = ProgressDialog.show(AvailabilityServiceManActivity.this, "",
														"Proszę czekać...", true);
												mAuthTask.execute((Void) null);
											}

										} catch (Exception e) {
											// TODO Auto-generated catch block
											new AlertDialog.Builder(AvailabilityServiceManActivity.this)
													.setMessage("Wystąpił błąd.")
													.setPositiveButton(android.R.string.ok, new OnClickListener() {
														@Override
														public void onClick(DialogInterface arg0, int arg1) {
															// TODO
															// Auto-generated
															finish();
														}
													}).show();
											e.printStackTrace();
										}
									} else {
										new AlertDialog.Builder(AvailabilityServiceManActivity.this)
												.setMessage("Upewnij się, że masz aktywne połączenie z Internetem.")
												.setPositiveButton(android.R.string.ok, new OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														// TODO Auto-generated
														// method stub
													}
												}).show();
									}

								}

							}
						}).setNegativeButton(android.R.string.cancel, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
							}
						}).show();

			}
		});

		myGetGPSLocationBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isGooglePSA()) {
					boolean isConnected = isNetworkConnected();
					if (isConnected) {

						myCity.setText("");
						myStreet.setText("");
						myNumber.setText("");
						myWorking_range.setText("");

						gps = new GPSTracker(AvailabilityServiceManActivity.this);
						if (gps.canGetLocation()) {

							double latitudeD = gps.getLatitude();
							double longitudeD = gps.getLongitude();
							latitude = Double.toString(latitudeD);
							longitude = Double.toString(longitudeD);

							try {

								List<Address> returnedaddresses = geocoder.getFromLocation(latitudeD, longitudeD,
										maxResult);

								if (returnedaddresses != null) {
									// city =
									// String.valueOf(returnedaddresses.get(0).getAddressLine(1));
									city = String.valueOf(returnedaddresses.get(0).getLocality());
									// street =
									// String.valueOf(returnedaddresses.get(0).getAddressLine(0));
									street = String.valueOf(returnedaddresses.get(0).getThoroughfare());
									number = String.valueOf(returnedaddresses.get(0).getFeatureName());

									// mAuthTask.execute((Void) null);
								}
								myCity.setText(city);
								myStreet.setText(street);
								myNumber.setText(number);

							} catch (Exception e) {
								// TODO Auto-generated catch block
								dialog.dismiss();
								new AlertDialog.Builder(AvailabilityServiceManActivity.this)
										.setMessage("Wprowadziles zle dane")
										.setPositiveButton(android.R.string.ok, new OnClickListener() {
											@Override
											public void onClick(DialogInterface arg0, int arg1) {
												// TODO Auto-generated method
												// stub
												finish();
											}
										}).show();
								e.printStackTrace();
							}
						} else {
							// can't get location
							// GPS or Network is not enabled
							// Ask user to enable GPS/network in settings
							gps.showSettingsAlert();
						}

					} else {
						Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT)
								.show();
					}

				}
			}
		});

		myMapViewLocationBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isGooglePSA()) {
					if (gps == null) {
						gps = new GPSTracker(AvailabilityServiceManActivity.this);
					}
					if (gps.canGetLocation()) {

						double latitudeD = gps.getLatitude();
						double longitudeD = gps.getLongitude();
						latitude = Double.toString(latitudeD);
						longitude = Double.toString(longitudeD);

						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + latitude + "," + longitude
								+ "?z=17"));

						startActivity(intent);

					} else {
						// can't get location
						// GPS or Network is not enabled
						// Ask user to enable GPS/network in settings
						gps.showSettingsAlert();
					}
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		getMenuInflater().inflate(R.menu.localization_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.localization_map:

			if (isGooglePSA()) {
				if (gps == null) {
					gps = new GPSTracker(AvailabilityServiceManActivity.this);
				}
				if (gps.canGetLocation()) {

					double latitudeD = gps.getLatitude();
					double longitudeD = gps.getLongitude();
					latitude = Double.toString(latitudeD);
					longitude = Double.toString(longitudeD);

					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + latitude + "," + longitude
							+ "?z=17"));

					startActivity(intent);

				} else {
					// can't get location
					// GPS or Network is not enabled
					// Ask user to enable GPS/network in settings
					gps.showSettingsAlert();
				}
			}

			break;

		default:
			break;
		}
		return true;
	}

	public boolean isGooglePSA() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if (resultCode == ConnectionResult.SUCCESS) {
			return true;
		} else if (resultCode == ConnectionResult.SERVICE_MISSING
				|| resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED
				|| resultCode == ConnectionResult.SERVICE_DISABLED) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000);
			dialog.show();
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

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class SaveAvailabilityTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
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
				return false;
			}

			return HttpResponseAuthorizationJSONService.authResponseFromJSON(response);

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			// showProgress(false);

			if (success) {
				// finish();
				dialog.dismiss();
				new AlertDialog.Builder(AvailabilityServiceManActivity.this)
						.setMessage("Zamiana lokalizacji zatwierdzona")
						.setPositiveButton(android.R.string.ok, new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								handler.sendEmptyMessage(1);
							}
						}).show();
			} else {
				dialog.dismiss();
				new AlertDialog.Builder(AvailabilityServiceManActivity.this)
						.setMessage("Zamiana lokalizacji nie została zatwierdzona")
						.setPositiveButton(android.R.string.ok, new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
							}
						}).show();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			// showProgress(false);
		}
	}

	public class MyConfigurator implements ConnectionConfigurator {

		public void configure(HttpURLConnection connection) {

			// add custom HTTP header
			connection.addRequestProperty("Token", userDetailsMap.get(SessionManager.KEY_TOKEN));
		}
	}
}
