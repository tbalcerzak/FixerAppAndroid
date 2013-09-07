package pl.exorigoupos.fixerapp;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import pl.exorigoupos.fixerapp.data.Constants;
import pl.exorigoupos.fixerapp.json.UserJSONParsingService;
import pl.exorigoupos.fixerapp.services.AutoTrackingService;
import pl.exorigoupos.fixerapp.sessions.SessionManager;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

public class MainActivity extends FragmentActivity {

	private TabHost tabHost;
	private TabSpec noweSpec;
	private TabSpec zlozoneSpec;
	private TabSpec katalogSpec;
	private TabSpec zakonczoneSpec;
	private SessionManager sManager;
	private HashMap<String, String> userDetailsMap;
	private boolean isUserAuth = false;
	private static Handler handler;
	private ProgressDialog dialog;
	private UserRefreshLoginTask mAuthTask = null;
	ActionBar actionBar;
	ActionBar.OnNavigationListener mOnNavigationListener;
	private int numer = 1;
	int kategory = 0;
	int spinnerPosition = 0;
	private SharedPreferences sharedPref;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putInt("spinnerPosition", spinnerPosition);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		switch (resultCode) {
		case RESULT_OK:
			showDropDownListActionBar(spinnerPosition);
			break;
		case RESULT_CANCELED:
			break;
		default:
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			this.spinnerPosition = savedInstanceState.getInt("spinnerPosition");
		}
		setContentView(R.layout.activity_main);
		boolean isConnected = isNetworkConnected();
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case 1:
					sManager.setUserDetails(FixerApplication.user.getToken(),
							FixerApplication.user.getWorking_address_city(),
							FixerApplication.user.getWorking_address_street(),
							FixerApplication.user.getWorking_address_number(),
							FixerApplication.user.getWorking_range(), FixerApplication.user.getId());
					// tabHost.addTab(noweSpec); // Adding photos tab
					// tabHost.addTab(zlozoneSpec); // Adding songs tab
					// tabHost.addTab(katalogSpec); // Adding songs tab
					// tabHost.addTab(zakonczoneSpec); // Adding songs tab
					dialog.dismiss();
					break;
				case 2:
					dialog.dismiss();
					break;
				}
			}
		};

		sharedPref = this.getSharedPreferences("DevicePref", Context.MODE_PRIVATE);
		mAuthTask = new UserRefreshLoginTask();
		sManager = new SessionManager(getApplicationContext());
		userDetailsMap = sManager.getUserDetails();

		showDropDownListActionBar(spinnerPosition);
		tabHost = (TabHost) this.findViewById(R.id.tabHost);
		LocalActivityManager mLocalActivityManager = new LocalActivityManager(this, false);
		mLocalActivityManager.dispatchCreate(savedInstanceState);
		tabHost.setup(mLocalActivityManager);

		// tabHost.getTabWidget().setStripEnabled(false);
		// Tab for Photos
		noweSpec = tabHost.newTabSpec("Nowe");
		// setting Title and Icon for the Tab
		noweSpec.setIndicator("Nowe");
		Intent noweIntent = new Intent(this, ZgloszeniaListaActivity.class);
		noweIntent.putExtra("kategoria", 2);
		noweIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		noweSpec.setContent(noweIntent);
		zlozoneSpec = tabHost.newTabSpec("Złożone");
		// setting Title and Icon for the Tab
		zlozoneSpec.setIndicator("Złożone");
		Intent zlozoneIntent = new Intent(this, ZgloszeniaListaActivity.class);
		zlozoneIntent.putExtra("kategoria", 3);
		zlozoneIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		zlozoneSpec.setContent(zlozoneIntent);
		katalogSpec = tabHost.newTabSpec("Realizacja");
		// setting Title and Icon for the Tab
		katalogSpec.setIndicator("Realizacja");
		Intent katalogIntent = new Intent(this, ZgloszeniaListaActivity.class);
		katalogIntent.putExtra("kategoria", 1);
		katalogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		katalogSpec.setContent(katalogIntent);
		zakonczoneSpec = tabHost.newTabSpec("Zakończone");
		// setting Title and Icon for the Tab
		zakonczoneSpec.setIndicator("Zakończone");
		Intent zakonczoneIntent = new Intent(this, ZgloszeniaListaActivity.class);
		zakonczoneIntent.putExtra("kategoria", 4);
		zakonczoneIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		zakonczoneSpec.setContent(zakonczoneIntent);
		// Adding all TabSpec to TabHost
		if (savedInstanceState == null) {
			if (sManager.isLoggedIn()) {
				if (isConnected) {
					if (sManager.isFirstLoggedIn()) {
						// tabHost.addTab(noweSpec); // Adding photos tab
						// tabHost.addTab(zlozoneSpec); // Adding songs tab
						// tabHost.addTab(katalogSpec); // Adding songs tab
						// tabHost.addTab(zakonczoneSpec); // Adding songs tab
						sManager.setFirstLoggedIn(false);
					} else {
						dialog = ProgressDialog
								.show(MainActivity.this, "Aktualizacja danych", "Proszę czekać...", true);
						numer = 1;
						mAuthTask.execute((Void) null);
					}
				} else {
					new AlertDialog.Builder(MainActivity.this)
							.setMessage("Upewnij się, że masz aktywne połączenie z Internetem.")
							.setPositiveButton(android.R.string.ok, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
								}
							}).show();
				}
			} else {
				// user is not logged in redirect him to Login Activity
				Intent i = new Intent(MainActivity.this, LoginActivity.class);
				// Closing all the Activities
				// Staring Login Activity
				startActivity(i);
				super.finish();
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void showDropDownListActionBar(int positionItem) {
		// On Honeycomb sdfsdfMR2 we have the ViewPropertyAnimator APIs, which
		// allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			numer = 2;
			actionBar = getActionBar();
			// actionBar.setBackgroundDrawable(new
			// ColorDrawable(Color.parseColor("#F4F3ED")));
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

			mOnNavigationListener = new ActionBar.OnNavigationListener() {

				@Override
				public boolean onNavigationItemSelected(int itemPosition, long itemId) {
					// TODO Auto-generated method stub

					switch (itemPosition) {
					case 0:
						spinnerPosition = 0;
						kategory = 2;
						break;
					case 1:
						spinnerPosition = 1;
						kategory = 3;
						break;
					case 2:
						spinnerPosition = 2;
						kategory = 1;
						break;
					case 3:
						spinnerPosition = 3;
						kategory = 4;
						break;
					default:
						break;
					}
					boolean isConnected = isNetworkConnected();
					if (isConnected) {
						getOrderListFragment(kategory);
					} else {
						Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT)
								.show();
					}

					return false;
				}
			};
			SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.order_category_list,
					android.R.layout.simple_spinner_dropdown_item);
			actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
			actionBar.setSelectedNavigationItem(positionItem);
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			numer = 1;
		}
	}

	public boolean isNetworkConnected() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo == null) {
			return false;
		}
		return activeNetworkInfo.isConnected();
	}

	public void getOrderListFragment(int category) {
		OrderListFragment orderList = new OrderListFragment();
		Bundle args = new Bundle();
		args.putInt("kategoria", category);
		orderList.setArguments(args);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, orderList);
		transaction.addToBackStack(null);

		transaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
			getMenuInflater().inflate(R.menu.main, menu);
		} else {
			getMenuInflater().inflate(R.menu.new_main, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.lokalizacja_serviceman:
			startActivity(new Intent(this, AvailabilityServiceManActivity.class));
			break;
		case R.id.logout:
			sManager.logoutUser();
			Intent service = new Intent(MainActivity.this, AutoTrackingService.class);
			stopService(service);
			super.finish();
			break;
		case R.id.action_refresh_list:
			boolean isConnected = isNetworkConnected();
			if (isConnected) {
				getOrderListFragment(kategory);
			} else {
				Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
		return true;
	}

	public void close() {
		super.finish();
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(MainActivity.this).setMessage("Czy chcesz wyjsc?")
				.setPositiveButton(android.R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						close();
					}
				}).setNegativeButton(android.R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				}).show();

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserRefreshLoginTask extends AsyncTask<Void, Void, Boolean> {

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
			// Construct new request
			mySession.setConnectionConfigurator(new ConnectionConfigurator() {

				@Override
				public void configure(HttpURLConnection conncetion) {
					// TODO Auto-generated method stub
					// conncetion.setReadTimeout(2000);
				}
			});
			String method = "authUser";
			String email;
			String password;
			String deviceToken = sharedPref.getString("deviceToken", "");
			email = userDetailsMap.get(SessionManager.KEY_EMAIL);
			password = userDetailsMap.get(SessionManager.KEY_PASSWORD);
			int requestID = 0;

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("email", email);
			parameters.put("password", password);
			parameters.put("device", deviceToken);

			JSONRPC2Request request = new JSONRPC2Request(method, parameters, requestID);
			Log.e("Request", request.toString());
			// Send request
			JSONRPC2Response response = null;

			try {
				response = mySession.send(request);

			} catch (JSONRPC2SessionException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				Log.e("AAA", e.getMessage(), e);
			}

			if (response.indicatesSuccess()) {
				System.out.println(response.getResult());
			} else {
				System.out.println(response.getError().getMessage());
			}

			UserJSONParsingService service = new UserJSONParsingService();
			try {
				isUserAuth = service.authAndCreateUserFromJSON(response);
				return isUserAuth;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return isUserAuth;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			// showProgress(false);

			if (success) {

				handler.sendEmptyMessage(numer);

			} else {
				dialog.dismiss();
			}
		}

		@Override
		protected void onCancelled() {
			// showProgress(false);
		}
	}
}
