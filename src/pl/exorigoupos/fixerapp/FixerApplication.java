package pl.exorigoupos.fixerapp;

import pl.exorigoupos.fixerapp.model.User;
import android.app.Application;
import android.os.StrictMode;

public class FixerApplication extends Application {

	public static User user;

	@Override
	public void onCreate() {
		super.onCreate();
		user = new User();
	}
}
