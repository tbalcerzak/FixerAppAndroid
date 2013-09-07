package pl.exorigoupos.fixerapp.sessions;

import java.util.HashMap;

import pl.exorigoupos.fixerapp.LoginActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {

	SharedPreferences pref;
	Editor editor;

	Context _context;
	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "AndroidHivePref";

	// All Shared Preferences Keys
	private static final String IS_LOGIN = "IsLoggedIn";

	private static final String IS_FIRST_LOGIN = "IsFirstLoggedin";

	// User name (make variable public to access from outside)
	public static final String KEY_PASSWORD = "password";

	// Email address (make variable public to access from outside)
	public static final String KEY_EMAIL = "email";
	public static final String KEY_TOKEN = "token";
	public static final String KEY_ID = "id";
	public static final String KEY_CITY = "city";
	public static final String KEY_STREET = "street";
	public static final String KEY_NUMBER = "number";
	public static final String KEY_WORKING_RANGE = "working_range";
	public static final String KEY_AUTO_TRACKING = "auto_tracking";
	public static final String KEY_AUTO_TRACKING_TIME = "auto_tracking_time";

	// Constructor
	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
		// editor.commit();
	}

	public void setUserDetails(String token, String city, String street, String number, String working_range, String id) {

		editor.putString(KEY_TOKEN, token);
		editor.putString(KEY_CITY, city);
		editor.putString(KEY_STREET, street);
		editor.putString(KEY_NUMBER, number);
		editor.putString(KEY_WORKING_RANGE, working_range);
		editor.putString(KEY_ID, id);

		// commit changes
		editor.commit();
	}

	public void setUserRange(String working_range) {
		editor.putString(KEY_WORKING_RANGE, working_range);
		editor.commit();
	}

	public void setUserCity(String city) {
		editor.putString(KEY_CITY, city);
		editor.commit();
	}

	public void setUserStreet(String street) {
		editor.putString(KEY_STREET, street);
		editor.commit();
	}

	public void setUserNumber(String number) {
		editor.putString(KEY_NUMBER, number);
		editor.commit();
	}

	public void setAutoTracking(boolean isTracking) {
		editor.putBoolean(KEY_AUTO_TRACKING, isTracking);
		editor.commit();
	}

	public void setAutoTrackingTime(int spinnerItemPosition) {
		editor.putInt(KEY_AUTO_TRACKING_TIME, spinnerItemPosition);
		editor.commit();
	}

	public int getAutoTrackingTime() {
		return pref.getInt(KEY_AUTO_TRACKING_TIME, 0);
	}

	/**
	 * Create login session
	 * */
	public void createLoginSession(String email, String password) {
		// Storing login value as TRUE
		editor.putBoolean(IS_LOGIN, true);
		editor.putBoolean(IS_FIRST_LOGIN, true);
		// Storing name in pref
		editor.putString(KEY_EMAIL, email);

		// Storing email in pref
		editor.putString(KEY_PASSWORD, password);

		// commit changes
		editor.commit();

	}

	/**
	 * Get stored session data
	 * */
	public HashMap<String, String> getUserDetails() {

		HashMap<String, String> user = new HashMap<String, String>();
		user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, ""));
		user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, ""));
		user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, ""));
		user.put(KEY_CITY, pref.getString(KEY_CITY, ""));
		user.put(KEY_STREET, pref.getString(KEY_STREET, ""));
		user.put(KEY_NUMBER, pref.getString(KEY_NUMBER, ""));
		user.put(KEY_WORKING_RANGE, pref.getString(KEY_WORKING_RANGE, ""));
		user.put(KEY_ID, pref.getString(KEY_ID, ""));
		// user.put(KEY_AUTO_TRACKING, pref.getBoolean(KEY_AUTO_TRACKING,
		// false));

		// return user
		return user;
	}

	/**
	 * Check login method wil check user login status If false it will redirect
	 * user to login page Else won't do anything
	 * */
	public boolean checkLogin() {
		// Check login status
		if (!this.isLoggedIn()) {
			// user is not logged in redirect him to Login Activity
			Intent i = new Intent(_context, LoginActivity.class);
			// Closing all the Activities
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			// Add new Flag to start new Activity
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// Staring Login Activity
			_context.startActivity(i);
		}
		return true;
	}

	/**
	 * Clear session details
	 * */
	public void logoutUser() {
		// Clearing all data from Shared Preferences
		editor.clear();
		editor.commit();

		// After logout redirect user to Loing Activity
		Intent i = new Intent(_context, LoginActivity.class);
		// Closing all the Activities
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// Add new Flag to start new Activity
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// Staring Login Activity
		_context.startActivity(i);
	}

	/**
	 * Quick check for login
	 * **/
	// Get Login State
	public boolean isLoggedIn() {
		return pref.getBoolean(IS_LOGIN, false);
	}

	public boolean isAutoTracking() {
		return pref.getBoolean(KEY_AUTO_TRACKING, false);
	}

	public boolean isFirstLoggedIn() {
		return pref.getBoolean(IS_FIRST_LOGIN, false);
	}

	public void setFirstLoggedIn(boolean first) {
		editor.putBoolean(IS_FIRST_LOGIN, first);
		editor.commit();
	}
}
