package pl.exorigoupos.fixerapp;

import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.exorigoupos.fixerapp.data.Constants;
import pl.exorigoupos.fixerapp.json.HttpResponseAuthorizationJSONService;
import pl.exorigoupos.fixerapp.json.OrderJSONParsingService;
import pl.exorigoupos.fixerapp.model.Order;
import pl.exorigoupos.fixerapp.sessions.SessionManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

public class ZgloszeniaListaActivity extends Activity {
	protected ListView listView;
	private List<Order> ordersList;
	private GetServicesListTask mAuthTask = null;
	private ProgressBar bar;
	final int REFRESH = 1;
	private HashMap<String, String> userDetailsMap;
	private SessionManager sManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_order_list);

		sManager = new SessionManager(getApplicationContext());
		userDetailsMap = sManager.getUserDetails();

		listView = (ListView) findViewById(android.R.id.list);
		bar = (ProgressBar) findViewById(R.id.progressBar);
		mAuthTask = new GetServicesListTask();
		mAuthTask.execute((Void) null);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				listView.refreshDrawableState();
				Order o = ordersList.get(position);

				Intent intent = new Intent(ZgloszeniaListaActivity.this, ShowOrderActivity.class);
				Bundle b = o.toBundle();
				intent.putExtras(b);
				startActivityForResult(intent, REFRESH);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		switch (resultCode) {
		case RESULT_OK:
			mAuthTask = new GetServicesListTask();
			mAuthTask.execute((Void) null);
			break;

		default:
			break;
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class GetServicesListTask extends AsyncTask<Void, Void, Boolean> {
		String method;

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.,
			// bar.setVisibility(View.VISIBLE);
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
			// Construct new request
			mySession.setConnectionConfigurator(new MyConfigurator());
			// method = "canSeeOffers";
			method = "getServicemanServices";
			String servicemanId = userDetailsMap.get(SessionManager.KEY_ID);
			int requestID = 0;

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("servicemanId", servicemanId);
			parameters.put("type", getIntent().getExtras().getInt("kategoria") + "");

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

			// // Print response result / error
			// if
			// (HttpResponseAuthorizationJSONService.authResponseFromJSON(response))
			// {
			// System.out.println(response.getResult());
			// method = "getServicemanServices";
			// request = new JSONRPC2Request(method, parameters, requestID);
			// try {
			// response = mySession.send(request);
			//
			// } catch (JSONRPC2SessionException e) {
			// System.err.println(e.getMessage());
			// e.printStackTrace();
			// Log.e("AAA", e.getMessage(), e);
			// return false;
			// }
			//
			// } else {
			// System.out.println(response.getError().getMessage());
			// }

			// Print response result / error
			if (HttpResponseAuthorizationJSONService.authResponseFromJSON(response)) {
				// Log.e("json", response.getResult().toString());
				System.out.println(response.getResult());

				OrderJSONParsingService service = new OrderJSONParsingService();
				try {
					ordersList = service.createOrderListFromJSON(response);
					return true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			} else {
				System.out.println(response.getError().getMessage());
				return false;
			}

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;

			if (success) {
				ItemAdapter adapter = new ItemAdapter(ordersList);
				((ListView) listView).setAdapter(adapter);
				bar.setVisibility(View.GONE);

			} else {
				bar.setVisibility(View.GONE);
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
		}
	}

	class ItemAdapter extends BaseAdapter {

		private List<Order> itemArrayList;

		public ItemAdapter(List<Order> results) {
			itemArrayList = results;
		}

		private class ViewHolder {
			TextView txt_id_zgloszenia;
			TextView txt_name;
			TextView txt_termin_realizacji;
			TextView txt_adres;
		}

		@Override
		public int getCount() {
			return itemArrayList.size();
		}

		@Override
		public Object getItem(int position) {
			return itemArrayList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.item_list_zlecenie_nowe, parent, false);
				holder = new ViewHolder();
				holder.txt_id_zgloszenia = (TextView) view.findViewById(R.id.txt_dedline);
				holder.txt_name = (TextView) view.findViewById(R.id.txt_nazwaZlecenia);
				holder.txt_termin_realizacji = (TextView) view.findViewById(R.id.txt_termin_realizacji);
				holder.txt_adres = (TextView) view.findViewById(R.id.txt_adres);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.txt_id_zgloszenia.setText("Zgłoszenie " + itemArrayList.get(position).getId());
			holder.txt_name.setText(itemArrayList.get(position).getName());
			holder.txt_termin_realizacji.setText("Termin realizacji: "
					+ itemArrayList.get(position).getRealization_date() + " Godziny: "
					+ itemArrayList.get(position).getRealization_time_range());
			holder.txt_adres.setText("Adres: " + itemArrayList.get(position).getAddress_city() + ", "
					+ itemArrayList.get(position).getAddress_street());
			return view;
		}
	}

	public class MyConfigurator implements ConnectionConfigurator {

		public void configure(HttpURLConnection connection) {

			// add custom HTTP header
			connection.addRequestProperty("Token", userDetailsMap.get(SessionManager.KEY_TOKEN));
			// connection.setReadTimeout(2000);
		}
	}
}
