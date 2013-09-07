package pl.exorigoupos.fixerapp;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.exorigoupos.fixerapp.ZgloszeniaListaActivity.GetServicesListTask;
import pl.exorigoupos.fixerapp.data.Constants;
import pl.exorigoupos.fixerapp.json.HttpResponseAuthorizationJSONService;
import pl.exorigoupos.fixerapp.json.OrderJSONParsingService;
import pl.exorigoupos.fixerapp.map.GPSTracker;
import pl.exorigoupos.fixerapp.model.Order;
import pl.exorigoupos.fixerapp.sessions.SessionManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

public class OrderListFragment extends Fragment {
	final static String ARG_POSITION = "position";
	int mCurrentPosition = -1;
	protected ListView listView;
	private List<Order> ordersList;

	boolean first = true;

	private TextView tvEmptyList;
	private GetServicesListTask mAuthTask = null;
	private ProgressBar bar;
	final int REFRESH = 1;
	private HashMap<String, String> userDetailsMap;
	private SessionManager sManager;

	public interface OnArticleSelectedListener {
		public void onArticleSelected(Uri articleUri);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);

		listView = (ListView) view.findViewById(android.R.id.list);
		tvEmptyList = (TextView) view.findViewById(R.id.emptyListTV);
		bar = (ProgressBar) view.findViewById(R.id.progressBar);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				listView.refreshDrawableState();
				Order o = ordersList.get(position);

				Intent intent = new Intent(getActivity(), ShowOrderActivity.class);
				Bundle b = o.toBundle();
				intent.putExtras(b);
				startActivityForResult(intent, REFRESH);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// If activity recreated (such as from screen rotate), restore
		// the previous article selection set by onSaveInstanceState().
		// This is primarily necessary when in the two-pane layout.
		if (savedInstanceState != null) {
			mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
		}

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.ac_order_list, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// During startup, check if there are arguments passed to the fragment.
		// onStart is a good place to do this because the layout has already
		// been
		// applied to the fragment at this point so we can safely call the
		// method
		// below that sets the article text.
		sManager = new SessionManager(getActivity());
		userDetailsMap = sManager.getUserDetails();
		if (first) {
			Bundle args = getArguments();
			if (args != null) {
				// Set article based on argument passed in
				updateArticleView(args.getInt("kategoria"));
				first = false;
			}
		}
	}

	public void updateArticleView(int position) {
		// TextView article = (TextView)
		// getActivity().findViewById(R.id.article);
		// article.setText(Ipsum.Articles[position]);
		// mCurrentPosition = position;
		mAuthTask = new GetServicesListTask();
		mAuthTask.execute((Void) null);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the
		// fragment
		outState.putInt(ARG_POSITION, mCurrentPosition);
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
			parameters.put("type", getArguments().getInt("kategoria") + "");

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
				ItemAdapter adapter = new ItemAdapter(ordersList, getActivity());
				((ListView) listView).setAdapter(adapter);
				if (ordersList.isEmpty()) {
					tvEmptyList.setVisibility(View.VISIBLE);
				}
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
		private LayoutInflater mLayoutInflater;
		private List<Order> itemArrayList;

		public ItemAdapter(List<Order> results, Context ctx) {
			itemArrayList = results;
			mLayoutInflater = LayoutInflater.from(ctx);
		}

		private class ViewHolder {
			TextView txt_dedline;
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
				view = mLayoutInflater.inflate(R.layout.item_list_zlecenie_nowe, parent, false);
				holder = new ViewHolder();
				holder.txt_dedline = (TextView) view.findViewById(R.id.txt_dedline);
				holder.txt_name = (TextView) view.findViewById(R.id.txt_nazwaZlecenia);
				holder.txt_termin_realizacji = (TextView) view.findViewById(R.id.txt_termin_realizacji);
				holder.txt_adres = (TextView) view.findViewById(R.id.txt_adres);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.txt_dedline.setText("Termin składania ofert " + itemArrayList.get(position).getOffer_dedline());
			holder.txt_name.setText(itemArrayList.get(position).getName());
			if (itemArrayList.get(position).getRealization_date().equals("null")) {
				holder.txt_termin_realizacji.setText("Termin realizacji: Jak najszybciej");
			} else {
				holder.txt_termin_realizacji.setText("Termin realizacji: "
						+ itemArrayList.get(position).getRealization_date() + ", godz. "
						+ itemArrayList.get(position).getRealization_time_range());
			}
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
