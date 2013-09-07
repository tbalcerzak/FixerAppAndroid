package pl.exorigoupos.fixerapp;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import pl.exorigoupos.fixerapp.data.Constants;
import pl.exorigoupos.fixerapp.json.HttpResponseAuthorizationJSONService;
import pl.exorigoupos.fixerapp.sessions.SessionManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

public class ServicemanOffertActivity extends Activity {
	private Bundle b;
	private TextView tvOrderName;
	private TextView tvAdress;
	private EditText etServicemanPrice;
	private EditText etServicemanDsc;
	private Button btnOffer;
	private Object tabKeys[];
	private PutOfferTask mAuthTask = null;
	private LinkedHashMap<String, String> mapa;
	private Object[] days;
	private String[] hours = { "-- wybierz godzine --", "6-8", "8-10", "10-12", "12-14", "14-16", "16-18", "18-20",
			"20-22", "22-24" };
	private String servicemanDay;
	private String servicemanTimeRange;
	private String servicemanPrice;
	private String servicemanDsc;
	private ProgressDialog dialog;
	private SessionManager sManager;
	private HashMap<String, String> userDetailsMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_serviceman_offert);
		sManager = new SessionManager(getApplicationContext());
		userDetailsMap = sManager.getUserDetails();
		Intent startingIntent = getIntent();
		if (startingIntent != null) {
			b = startingIntent.getExtras();
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();

		mapa = new LinkedHashMap<String, String>();
		mapa.put("0", "-- wybierz dzień --");
		mapa.put(dateFormat.format(cal.getTime()), "Dzisiaj");
		cal.add(Calendar.DAY_OF_YEAR, 1);
		mapa.put(dateFormat.format(cal.getTime()), "Jutro");
		cal.add(Calendar.DAY_OF_YEAR, 1);
		mapa.put(dateFormat.format(cal.getTime()), "Pojutrze");
		for (int i = 0; i < 4; i++) {
			cal.add(Calendar.DAY_OF_YEAR, 1);
			mapa.put(
					dateFormat.format(cal.getTime()),
					getDayName(cal.get(Calendar.DAY_OF_WEEK)) + ", " + cal.get(Calendar.DAY_OF_MONTH) + "-"
							+ (cal.get(Calendar.MONTH) + 1));
		}
		days = new String[mapa.size()];
		days = mapa.values().toArray();

		btnOffer = (Button) findViewById(R.id.btn_offer);
		etServicemanPrice = (EditText) findViewById(R.id.price);
		etServicemanPrice.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				etServicemanPrice.setFocusable(true);
				etServicemanPrice.setFocusableInTouchMode(true);
				return false;
			}
		});
		etServicemanDsc = (EditText) findViewById(R.id.dsc);
		etServicemanDsc.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				etServicemanDsc.setFocusable(true);
				etServicemanDsc.setFocusableInTouchMode(true);
				return false;
			}
		});
		Spinner spinDays = (Spinner) findViewById(R.id.spinnerDays);
		Spinner spinHours = (Spinner) findViewById(R.id.spinnerHours);
		tvOrderName = (TextView) findViewById(R.id.order_name);
		tvOrderName.setText("Tytuł oferty: " + b.getString("name"));
		tvAdress = (TextView) findViewById(R.id.adress);
		tvAdress.setText("Dane teleadresowe: " + b.getString("Address_city") + ", " + b.getString("Address_street"));

		ArrayAdapter adapterDays = new ArrayAdapter(this, android.R.layout.simple_spinner_item, days);
		adapterDays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ArrayAdapter adapterHours = new ArrayAdapter(this, android.R.layout.simple_spinner_item, hours);
		adapterHours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinHours.setAdapter(adapterHours);
		String client_time_range = b.getString("realization_time_range");
		for (int i = 0; i < hours.length; i++) {
			if (hours[i].equals(client_time_range)) {
				spinHours.setSelection(i);
				break;
			}
		}

		spinHours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				// TODO Auto-generated method stub
				if (position == 0) {
					servicemanTimeRange = String.valueOf(position);
				} else {
					servicemanTimeRange = hours[position];
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		spinDays.setAdapter(adapterDays);
		String client_date = b.getString("realization_date");
		mapa.get(client_date);
		tabKeys = mapa.keySet().toArray();
		for (int i = 0; i < tabKeys.length; i++) {
			if (tabKeys[i].equals(client_date)) {
				spinDays.setSelection(i);
				break;
			}
		}

		spinDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				// TODO Auto-generated method stub
				servicemanDay = tabKeys[position].toString();
				// tvAdress.setText(tabKeys[position].toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		btnOffer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog = ProgressDialog.show(ServicemanOffertActivity.this, "", "Proszę czekać...", true);
				servicemanPrice = etServicemanPrice.getText().toString();
				servicemanDsc = etServicemanDsc.getText().toString();
				mAuthTask = new PutOfferTask();
				mAuthTask.execute((Void) null);
			}
		});
	}

	public String getDayName(int dayNumber) {
		switch (dayNumber) {
		case 1:
			return "Niedziela";
		case 2:
			return "Poniedziałek";
		case 3:
			return "Wtorek";
		case 4:
			return "Środa";
		case 5:
			return "Czwartek";
		case 6:
			return "Piątek";
		case 7:
			return "Sobota";
		default:
			return "";
		}
	}

	public class PutOfferTask extends AsyncTask<Void, Void, Boolean> {
		private String method;

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			URL serverURL = null;

			try {
				serverURL = new URL(Constants.SERVER_URL);

			} catch (MalformedURLException e) {
				// handle exception...
			}
			// final String rpcuser = "fixer";
			// final String rpcpassword = "fixMe";
			//
			// Authenticator.setDefault(new Authenticator() {
			// protected PasswordAuthentication getPasswordAuthentication() {
			// return new PasswordAuthentication(rpcuser,
			// rpcpassword.toCharArray());
			// }
			// });

			// Create new JSON-RPC 2.0 client session
			JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
			mySession.setConnectionConfigurator(new MyConfigurator());
			// method = "canPutOffer";
			method = "putOffer";
			String servicemanId = userDetailsMap.get(SessionManager.KEY_ID);
			String orderId = b.getString("id");
			int requestID = 0;

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("servicemanId", servicemanId);
			parameters.put("orderId", orderId);
			parameters.put("price", servicemanPrice);
			parameters.put("date", servicemanDay);
			parameters.put("timeRange", servicemanTimeRange);
			parameters.put("time", "");
			parameters.put("offer_dsc", servicemanDsc);

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
			boolean authResponse = HttpResponseAuthorizationJSONService.authResponseFromJSON(response);
			if (authResponse) {
				System.out.println(response.getResult());
			} else {
				System.out.println(response.getError().getMessage());
				return false;
			}

			return authResponse;

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			// showProgress(false);

			if (success) {
				// finish();
				dialog.dismiss();
				new AlertDialog.Builder(ServicemanOffertActivity.this).setMessage("Oferta  wysłana.")
						.setPositiveButton(android.R.string.ok, new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								setResult(RESULT_OK);
								finish();
							}
						}).show();
			} else {
				dialog.dismiss();
				new AlertDialog.Builder(ServicemanOffertActivity.this).setMessage("Wystąpił błąd.")
						.setPositiveButton(android.R.string.ok, new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								setResult(RESULT_CANCELED);
								finish();
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
