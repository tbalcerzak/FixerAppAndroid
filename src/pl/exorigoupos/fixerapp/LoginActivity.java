package pl.exorigoupos.fixerapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import pl.exorigoupos.fixerapp.data.Constants;
import pl.exorigoupos.fixerapp.json.UserJSONParsingService;
import pl.exorigoupos.fixerapp.sessions.SessionManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */

	private SessionManager sManager;
	private boolean isUserAuth = false;

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private GoogleCloudMessaging gcm;
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private static Handler handler;
	private SharedPreferences sharedPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sManager = new SessionManager(getApplicationContext());
		setContentView(R.layout.activity_login);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// Set up the login form.

		sharedPref = this.getSharedPreferences("DevicePref", Context.MODE_PRIVATE);
		// sharedPref.edit().clear().commit();

		if (sharedPref.getBoolean(getString(R.string.first_launch), true)) {
			if (isGooglePSA()) {
				try {
					gcm = GoogleCloudMessaging.getInstance(this);
					registerTokenDeviceTask();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					new AlertDialog.Builder(this).setMessage("Wprowadziles zle dane")
							.setPositiveButton(android.R.string.ok, new OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// TODO Auto-generated
									finish();
								}
							}).show();
					e.printStackTrace();
				}
			}
		}

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					finish();
					sManager.createLoginSession(mEmailView.getText().toString(), mPasswordView.getText().toString());
					sManager.setUserDetails(FixerApplication.user.getToken(),
							FixerApplication.user.getWorking_address_city(),
							FixerApplication.user.getWorking_address_street(),
							FixerApplication.user.getWorking_address_number(),
							FixerApplication.user.getWorking_range(), FixerApplication.user.getId());
					startActivity(new Intent(LoginActivity.this, MainActivity.class));
					break;
				case 2:
					break;

				default:
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString("deviceToken", msg.getData().getString("deviceToken"));
					editor.commit();
					break;
				}
			}
		};

		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);
		mEmailView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mEmailView.setFocusable(true);
				mEmailView.setFocusableInTouchMode(true);
				return false;
			}
		});

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mPasswordView.setFocusable(true);
				mPasswordView.setFocusableInTouchMode(true);
				return false;
			}
		});
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean isConnected = isNetworkConnected();
				if (isConnected) {
					attemptLogin();
				} else {
					new AlertDialog.Builder(LoginActivity.this)
							.setMessage("Upewnij się, że masz aktywne połączenie z Internetem.")
							.setPositiveButton(android.R.string.ok, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
								}
							}).show();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		isGooglePSA();
	}

	public void onPause() {
		super.onPause();
		// SharedPreferences sharedPref =
		// this.getSharedPreferences("DevicePref", Context.MODE_PRIVATE);
		if (sharedPref.getBoolean(getString(R.string.first_launch), true)) {
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putBoolean(getString(R.string.first_launch), false);
			editor.commit();
		}
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public boolean isGooglePSA() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if (resultCode == ConnectionResult.SUCCESS) {
			return true;
		} else if (resultCode == ConnectionResult.SERVICE_MISSING
				|| resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED
				|| resultCode == ConnectionResult.SERVICE_DISABLED) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
			dialog.show();
			return false;
		}
		return false;

	}

	private void registerTokenDeviceTask() {
		new AsyncTask<Void, Void, Boolean>() {
			String token;

			@Override
			protected void onPostExecute(final Boolean success) {
				// TODO Auto-generated method stub

				Bundle bundle = new Bundle();
				bundle.putString("deviceToken", token);
				Message msg = new Message();
				msg.setData(bundle);
				handler.sendMessage(msg);

			}

			protected Boolean doInBackground(Void... params) {

				try {
					token = gcm.register(getString(R.string.project_number));
					Log.e("registrationId", token);
				} catch (IOException e) {
					Log.e("Registration Error", e.getMessage());
					return false;
				}
				return true;
			}
		}.execute(null, null, null);

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
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

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
			String method = "authUser";
			String email;
			String deviceToken = sharedPref.getString("deviceToken", "");
			// String deviceToken =
			// "APA91bFmbG-y7m6WwyuVU3WaRz35m-dzxLa37flxisVSHcj8jtOOsLlybS64MXlk";
			String password;
			email = mEmailView.getText().toString();
			password = mPasswordView.getText().toString();

			int requestID = 0;

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("email", email);
			parameters.put("password", password);
			parameters.put("device", deviceToken);

			JSONRPC2Request request = new JSONRPC2Request(method, parameters, requestID);
			Log.e("RequestLogin", request.toString());
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
				// Log.e("response", response.getResult().toString());
			} else {
				System.out.println(response.getError().getMessage());
				// Log.e("response", response.getError().getMessage());
			}

			UserJSONParsingService service = new UserJSONParsingService();
			try {
				isUserAuth = service.authAndCreateUserFromJSON(response);
				return isUserAuth;
				// prefs.setServicemanId(servicemanId);
				// prefs.save();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// handler.sendEmptyMessage(2);
				e.printStackTrace();
				return isUserAuth;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {

				handler.sendEmptyMessage(1);

			} else {
				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
