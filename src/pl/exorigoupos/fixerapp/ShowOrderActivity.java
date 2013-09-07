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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

public class ShowOrderActivity extends Activity {
	private Bundle b;
	private Button btnMap;
	private Button btnTakeOrder;
	private Button btnChangePrice;
	private ServicesTask mTask = null;
	private String sBMode;
	private ProgressDialog dialog;
	private Spinner spinnerAssets;
	private LinkedHashMap<String, String> assetsMap;
	private Object nameTab[];
	private HashMap<String, String> userDetailsMap;
	private SessionManager sManager;
	private String servicemanDay;
	private String servicemanTimeRange;
	private String servicemanPrice;
	private String servicemanDsc;
	private Object tabKeys[];
	private String[] hours = { "-- wybierz godzine --", "6-8", "8-10", "10-12", "12-14", "14-16", "16-18", "18-20",
			"20-22", "22-24" };

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case RESULT_OK:
			setResult(RESULT_OK);
			finish();
			break;
		case RESULT_CANCELED:
			setResult(RESULT_CANCELED);
			finish();
		default:
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_order);
		sManager = new SessionManager(getApplicationContext());
		userDetailsMap = sManager.getUserDetails();
		Intent startingIntent = getIntent();
		mTask = new ServicesTask();
		assetsMap = new LinkedHashMap<String, String>();
		if (startingIntent != null) {
			b = startingIntent.getExtras();
			assetsMap.put("Załączniki", "0");
			assetsMap.putAll((Map<? extends String, ? extends String>) b.getSerializable("assetsMap"));
			nameTab = assetsMap.keySet().toArray();

		}

		btnTakeOrder = (Button) findViewById(R.id.btn_offer);
		TextView tvAdress = (TextView) findViewById(R.id.adress);
		TextView tvPriceClient = (TextView) findViewById(R.id.price_client);

		// if (b.getString("price_client").equals("null")) {
		// tvPriceClient.setVisibility(View.INVISIBLE);
		// }
//		tvPriceClient.setText("Cena zlecającego: " + b.getString("price_client") + "zł");
		switch (Integer.parseInt(b.getString("Status_id"))) {

		case Constants.STATUS_ASIGNED_SERVICEMAN:
			btnChangePrice = (Button) findViewById(R.id.btn_change_price);

			// btnChangePrice.setVisibility(View.VISIBLE);
			btnChangePrice.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AlertDialog.Builder alert = new AlertDialog.Builder(ShowOrderActivity.this);

					alert.setTitle("Zmiana ceny");

					// Set an EditText view to get user input
					boolean isConnected = isNetworkConnected();
					if (isConnected) {

						LinearLayout layout = new LinearLayout(getBaseContext());
						final EditText newPrice = new EditText(ShowOrderActivity.this);
						final EditText desc = new EditText(ShowOrderActivity.this);
						layout.setOrientation(LinearLayout.VERTICAL);
						layout.addView(newPrice);
						layout.addView(desc);
						newPrice.setHint("Nowa cena");
						desc.setHint("Powód zmiany");
						alert.setView(layout);

						alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
							}
						});

						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// Canceled.
							}
						});

						final AlertDialog changePassDialog = alert.create();
						changePassDialog.show();
						changePassDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
								new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										newPrice.setError(null);
										boolean cancel = false;
										View focusView = null;
										if (TextUtils.isEmpty(newPrice.getText().toString())) {
											newPrice.setError("Pole nie może być puste");
											focusView = newPrice;
											cancel = true;
										}
										if (TextUtils.isEmpty(desc.getText().toString())) {
											desc.setError("Pole nie może być puste");
											focusView = desc;
											cancel = true;
										}
										if (cancel) {
											focusView.requestFocus();
										} else {

											String price = newPrice.getText().toString();
											String description = desc.getText().toString();
											changePassDialog.dismiss();
											dialog = ProgressDialog.show(ShowOrderActivity.this, "",
													"Proszę czekać...", true);
											mTask.execute((String) (Constants.SERVICE_CHANGE_ORDER_PRICE_INT + ""),
													Constants.SERVICE_CHANGE_ORDER_PRICE, b.getString("id"),
													description, price);
										}
									}
								});
					} else {
						Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT)
								.show();
					}
				}
			});

			btnTakeOrder.setText("Zakończ");
			btnTakeOrder.setVisibility(View.GONE);
			tvAdress.setText("Lokalizacja: " + b.getString("Address_city") + ", " + b.getString("Address_street") + " "
					+ b.getString("Address_street_number") + ", Telefon: " + b.getString("Address_telephone"));
			btnTakeOrder.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					boolean isConnected = isNetworkConnected();
					if (isConnected) {

						AlertDialog.Builder alert = new AlertDialog.Builder(ShowOrderActivity.this);

						alert.setTitle("Wprowadź KOD");

						// Set an EditText view to get user input
						final EditText input = new EditText(ShowOrderActivity.this);
						alert.setView(input);

						alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
							}
						});

						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// Canceled.
							}
						});

						final AlertDialog changePassDialog = alert.create();
						changePassDialog.show();
						changePassDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
								new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										input.setError(null);
										boolean cancel = false;
										View focusView = null;
										if (TextUtils.isEmpty(input.getText().toString())) {
											input.setError("Pole nie może być puste");
											focusView = input;
											cancel = true;
										}
										if (cancel) {
											focusView.requestFocus();
										} else {

											String value = input.getText().toString();
											changePassDialog.dismiss();
											dialog = ProgressDialog.show(ShowOrderActivity.this, "",
													"Proszę czekać...", true);
											mTask.execute((String) (Constants.SERVICE_CLOSE_ORDER_INT + ""),
													Constants.SERVICE_CLOSE_ORDER, b.getString("id"), value);
										}
									}
								});
					} else {
						Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT)
								.show();
					}
				}
			});

			break;
		case Constants.STATUS_FINISHED:
			btnTakeOrder.setVisibility(Button.GONE);
			tvAdress.setText("Dane teleadresowe: " + b.getString("Address_city") + ", " + b.getString("Address_street")
					+ " " + b.getString("Address_street_number") + ", Telefon: " + b.getString("Address_telephone"));
			break;
		case Constants.STATUS_IN_AUCTION:
			tvAdress.setText("Dane teleadresowe: " + b.getString("Address_city") + ", " + b.getString("Address_street"));
			if (b.getString("OrderServicemans_price").equals("null")) {
				btnTakeOrder.setText("Złóż ofertę");
				btnTakeOrder.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean isConnected = isNetworkConnected();
						if (isConnected) {
							Intent intent = new Intent(ShowOrderActivity.this, ServicemanOffertActivity.class);
							intent.putExtras(b);
							startActivityForResult(intent, 1);
						} else {
							Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT)
									.show();
						}
					}
				});
			} else {
				btnTakeOrder.setText("Anuluj ofertę");
				btnTakeOrder.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						AlertDialog.Builder alert = new AlertDialog.Builder(ShowOrderActivity.this);

						alert.setTitle("Anuluj ofertę");

						// Set an EditText view to get user input
						final EditText input = new EditText(ShowOrderActivity.this);
						input.setHint("Podaj przyczynę:");
						alert.setView(input);

						alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
							}
						});

						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// Canceled.
							}
						});

						final AlertDialog changePassDialog = alert.create();
						changePassDialog.show();
						changePassDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
								new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										input.setError(null);
										boolean cancel = false;
										View focusView = null;
										if (TextUtils.isEmpty(input.getText().toString())) {
											input.setError("Pole nie może być puste");
											focusView = input;
											cancel = true;
										}
										if (cancel) {
											focusView.requestFocus();
										} else {

											String value = input.getText().toString();
											changePassDialog.dismiss();
											dialog = ProgressDialog.show(ShowOrderActivity.this, "",
													"Proszę czekać...", true);
											mTask.execute((String) (Constants.SERVICE_CANCEL_OFFER_INT + ""),
													Constants.SERVICE_CANCEL_OFFER, b.getString("id"), value);
										}
									}
								});
					}
				});
			}

			break;
		case Constants.STATUS_SENT:
			btnTakeOrder.setText("Przyjmij ofertę");
			tvAdress.setText("Dane teleadresowe: " + b.getString("Address_city") + ", " + b.getString("Address_street"));
			btnTakeOrder.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					new AlertDialog.Builder(ShowOrderActivity.this).setMessage("Jestes pewien?")
							.setPositiveButton(android.R.string.ok, new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialogI, int which) {
									// TODO Auto-generated method stub
									dialog = ProgressDialog.show(ShowOrderActivity.this, "", "Proszę czekać...", true);
									mTask.execute((String) (Constants.SERVICE_REGISTER_SERVICEMAN_TO_SERVICE_INT + ""),
											Constants.SERVICE_REGISTER_SERVICEMAN_TO_SERVICE, b.getString("id"));
								}
							}).setNegativeButton(android.R.string.cancel, new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
								}
							}).show();
				}
			});
			break;

		default:
			btnTakeOrder.setVisibility(Button.GONE);
			break;
		}

		btnMap = (Button) findViewById(R.id.btn_map);
		btnMap.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + b.getString("Address_lat") + ","
						+ b.getString("Address_lng"))));

			}
		});

		TextView tvId = (TextView) findViewById(R.id.id);
		tvId.setText("Numer zgłoszenia: " + b.getString("id"));

		TextView tvOrderName = (TextView) findViewById(R.id.order_name);
		tvOrderName.setText(b.getString("name"));

		TextView tvBidMode = (TextView) findViewById(R.id.bid_mode);

		int intBMode = Integer.parseInt(b.getString("bid_mode"));

		switch (intBMode) {
		case 1:
			sBMode = Constants.BID_MODE_1;
			if (!b.getString("OrderServicemans_price").equals("null")) {
				// View view = (View)
				// findViewById(R.id.serviceman_offert_divider);
				// view.setVisibility(View.VISIBLE);
				TextView tvOffert = (TextView) findViewById(R.id.serviceman_offert);
				tvOffert.setVisibility(TextView.VISIBLE);
				TextView tvOffertDay = (TextView) findViewById(R.id.serviceman_realization_date);
				tvOffertDay.setText("Dzień: " + b.getString("OrderServicemans_realization_date") + ", godz."
						+ b.getString("OrderServicemans_realization_time_range"));
				tvOffertDay.setVisibility(TextView.VISIBLE);
				// TextView tvOffertTime = (TextView)
				// findViewById(R.id.serviceman_realization_time);
				// tvOffertTime.setText("Godzina: " +
				// b.getString("OrderServicemans_realization_time_range"));
				// tvOffertTime.setVisibility(TextView.VISIBLE);
				TextView tvOffertPrice = (TextView) findViewById(R.id.serviceman_price);
				tvOffertPrice.setText("Cena: " + b.getString("OrderServicemans_price") + " zł");
				tvOffertPrice.setVisibility(TextView.VISIBLE);
				TextView tvOffertDsc = (TextView) findViewById(R.id.serviceman_dsc);
				tvOffertDsc.setText("Uwagi: " + b.getString("OrderServicemans_offer_dsc"));
				tvOffertDsc.setVisibility(TextView.VISIBLE);
				tvPriceClient.setText("Cena: " + b.getString("price_client") + "zł");
				tvPriceClient.setVisibility(View.VISIBLE);
			}
			break;
		case 0:
			sBMode = Constants.BID_MODE_0;
			tvPriceClient.setText("Cena: " + b.getString("price_client") + "zł");
			tvPriceClient.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}

		tvBidMode.setText("Tryb zgłoszenia: " + sBMode);
		// }
		TextView tvCreatedAt = (TextView) findViewById(R.id.created_at);
		tvCreatedAt.setText("Opublikowano: " + b.getString("created_at"));

		TextView tvOfferDedline = (TextView) findViewById(R.id.offer_dedline);
		tvOfferDedline.setText("Termin składania ofert: " + b.getString("offer_dedline"));

		TextView tvCommentsDsc = (TextView) findViewById(R.id.problem_dsc);
		tvCommentsDsc.setText("Opis problemu: " + b.getString("problem_dsc"));

		TextView tvProblemsList = (TextView) findViewById(R.id.problems_list);

		if (nameTab.length > 1) {
			// tvProblemsList.setText("Usługi: " +
			// b.getStringArrayList("problemsList"));
			// tvProblemsList.setVisibility(View.VISIBLE);
			spinnerAssets = (Spinner) findViewById(R.id.spinner_assets);
			spinnerAssets.setVisibility(View.VISIBLE);

			ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, nameTab);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerAssets.setAdapter(adapter);

			spinnerAssets.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					// TODO Auto-generated method stub
					if (position != 0) {

						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SERVER_ADDRESS
								+ assetsMap.get(spinnerAssets.getSelectedItem().toString()))));
						// Toast.makeText(getBaseContext(),
						// assetsMap.get(spinnerAssets.getSelectedItem().toString()),
						// Toast.LENGTH_SHORT).show();
						spinnerAssets.setSelection(0);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {

				}
			});
		}
		TextView tvManufacturerName = (TextView) findViewById(R.id.Manufacturer_name);
		if (!b.getString("Unit_model").equals("")) {
			tvManufacturerName.setText("Urządzenie: " + b.getString("Manufacturer_name") + " "
					+ b.getString("Unit_model") + " " + b.getString("Unit_serial_no"));
			tvManufacturerName.setVisibility(View.VISIBLE);
		}

		TextView tvRealizationDate = (TextView) findViewById(R.id.realization_date);
		if (b.getString("realization_date").equals("null")) {
			tvRealizationDate.setText("Termin realizacji: Jak najszybciej");
		} else {
			tvRealizationDate.setText("Termin realizacji: " + b.getString("realization_date") + ", godz. "
					+ b.getString("realization_time_range"));
		}
		TextView tvTreeItemTitle = (TextView) findViewById(R.id.tree_item_title);
		tvTreeItemTitle.setText("Obszar serwisowy: " + b.getString("TreeItem_title"));

//		TextView tvProof = (TextView) findViewById(R.id.proof_of_purchase);
//		if (b.getString("proof_of_purchase").equals("1")) {
//			tvProof.setVisibility(View.VISIBLE);
//			tvProof.setText("Potrzebny dowód zakupu usługi");
//		}
		TextView tvFV = (TextView) findViewById(R.id.fv);
		if (b.getString("proof_of_purchase").equals("1")) {
			tvFV.setVisibility(View.VISIBLE);
		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.put_offer:
			if (isNetworkConnected()) {
				// Intent intent = new Intent(ShowOrderActivity.this,
				// ServicemanOffertActivity.class);
				// intent.putExtras(b);
				// startActivityForResult(intent, 1);
				putOfferView();
			} else {
				Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.show_map_order:
			if (isNetworkConnected()) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + b.getString("Address_lat") + ","
						+ b.getString("Address_lng"))));
			} else {
				Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.cancel_offer:
			AlertDialog.Builder alertCancel = new AlertDialog.Builder(ShowOrderActivity.this);
			alertCancel.setTitle("Anuluj ofertę nr " + b.getString("id"));
			// Set an EditText view to get user input
			final EditText input = new EditText(ShowOrderActivity.this);
			input.setHint("Podaj przyczynę:");
			alertCancel.setView(input);

			alertCancel.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});

			alertCancel.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Canceled.
				}
			});

			final AlertDialog cancelOfferDialog = alertCancel.create();
			cancelOfferDialog.show();
			cancelOfferDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					input.setError(null);
					boolean cancel = false;
					View focusView = null;
					if (TextUtils.isEmpty(input.getText().toString())) {
						input.setError("Pole nie może być puste");
						focusView = input;
						cancel = true;
					}
					if (cancel) {
						focusView.requestFocus();
					} else {
						String value = input.getText().toString();
						cancelOfferDialog.dismiss();
						dialog = ProgressDialog.show(ShowOrderActivity.this, "", "Proszę czekać...", true);
						mTask.execute((String) (Constants.SERVICE_CANCEL_OFFER_INT + ""),
								Constants.SERVICE_CANCEL_OFFER, b.getString("id"), value);
					}
				}
			});
			break;
		case R.id.change_price:
			AlertDialog.Builder alert = new AlertDialog.Builder(ShowOrderActivity.this);

			alert.setTitle("Zmiana ceny");

			// Set an EditText view to get user input
			if (isNetworkConnected()) {

				LinearLayout layout = new LinearLayout(getBaseContext());
				final EditText newPrice = new EditText(ShowOrderActivity.this);
				final EditText desc = new EditText(ShowOrderActivity.this);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.addView(newPrice);
				layout.addView(desc);
				newPrice.setHint("Nowa cena");
				desc.setHint("Powód zmiany");
				alert.setView(layout);

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

				final AlertDialog changePassDialog = alert.create();
				changePassDialog.show();
				changePassDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						newPrice.setError(null);
						boolean cancel = false;
						View focusView = null;
						if (TextUtils.isEmpty(newPrice.getText().toString())) {
							newPrice.setError("Pole nie może być puste");
							focusView = newPrice;
							cancel = true;
						}
						if (TextUtils.isEmpty(desc.getText().toString())) {
							desc.setError("Pole nie może być puste");
							focusView = desc;
							cancel = true;
						}
						if (cancel) {
							focusView.requestFocus();
						} else {

							String price = newPrice.getText().toString();
							String description = desc.getText().toString();
							changePassDialog.dismiss();
							dialog = ProgressDialog.show(ShowOrderActivity.this, "", "Proszę czekać...", true);
							mTask.execute((String) (Constants.SERVICE_CHANGE_ORDER_PRICE_INT + ""),
									Constants.SERVICE_CHANGE_ORDER_PRICE, b.getString("id"), description, price);
						}
					}
				});
			} else {
				Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.close_order:
			if (isNetworkConnected()) {

				AlertDialog.Builder alertClose = new AlertDialog.Builder(ShowOrderActivity.this);
				alertClose.setTitle("Wprowadź KOD");

				// Set an EditText view to get user input
				final EditText input2 = new EditText(ShowOrderActivity.this);
				alertClose.setView(input2);

				alertClose.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

				alertClose.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

				final AlertDialog changePassDialog = alertClose.create();
				changePassDialog.show();
				changePassDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						input2.setError(null);
						boolean cancel = false;
						View focusView = null;
						if (TextUtils.isEmpty(input2.getText().toString())) {
							input2.setError("Pole nie może być puste");
							focusView = input2;
							cancel = true;
						}
						if (cancel) {
							focusView.requestFocus();
						} else {

							String value = input2.getText().toString();
							changePassDialog.dismiss();
							dialog = ProgressDialog.show(ShowOrderActivity.this, "", "Proszę czekać...", true);
							mTask.execute((String) (Constants.SERVICE_CLOSE_ORDER_INT + ""),
									Constants.SERVICE_CLOSE_ORDER, b.getString("id"), value);
						}
					}
				});
			} else {
				Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		Bundle b = getIntent().getExtras();

		switch (Integer.parseInt(b.getString("Status_id"))) {
		case Constants.STATUS_ASIGNED_SERVICEMAN:
			getMenuInflater().inflate(R.menu.serviceman_offer_asigned, menu);
			break;
		case Constants.STATUS_FINISHED:
			getMenuInflater().inflate(R.menu.serviceman_offer_finish, menu);
			break;
		case Constants.STATUS_IN_AUCTION:
			if (b.getString("OrderServicemans_price").equals("null")) {
				getMenuInflater().inflate(R.menu.serviceman_offer_auction, menu);
				break;
			} else {
				getMenuInflater().inflate(R.menu.serviceman_offer_auction_cancel, menu);
				break;
			}

		default:

			break;
		}
		return true;
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

	public void putOfferView() {

		LinkedHashMap<String, String> mapa;
		Object[] days;
		SessionManager sManager;
		HashMap<String, String> userDetailsMap;
		sManager = new SessionManager(getApplicationContext());
		userDetailsMap = sManager.getUserDetails();
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

		Spinner spinDays = new Spinner(ShowOrderActivity.this);
		Spinner spinHours = new Spinner(ShowOrderActivity.this);
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

		AlertDialog.Builder alert = new AlertDialog.Builder(ShowOrderActivity.this);
		alert.setTitle("Złóż ofertę");
		LinearLayout layout = new LinearLayout(getBaseContext());
		final EditText price = new EditText(ShowOrderActivity.this);
		final EditText desc = new EditText(ShowOrderActivity.this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(spinDays);
		layout.addView(spinHours);
		layout.addView(price);
		layout.addView(desc);
		price.setHint("Cena");
		desc.setHint("Komentarz");
		alert.setView(layout);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		final AlertDialog changePassDialog = alert.create();
		changePassDialog.show();
		changePassDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				price.setError(null);
				boolean cancel = false;
				View focusView = null;
				if (TextUtils.isEmpty(price.getText().toString())) {
					price.setError("Pole nie może być puste");
					focusView = price;
					cancel = true;
				}
//				if (TextUtils.isEmpty(desc.getText().toString())) {
//					desc.setError("Pole nie może być puste");
//					focusView = desc;
//					cancel = true;
//				}
				if (cancel) {
					focusView.requestFocus();
				} else {

					String priceOffert = price.getText().toString();
					String description = desc.getText().toString();
					changePassDialog.dismiss();
					dialog = ProgressDialog.show(ShowOrderActivity.this, "", "Proszę czekać...", true);
					mTask.execute((String) (Constants.SERVICE_PUT_OFFER_INT + ""), Constants.SERVICE_PUT_OFFER,
							b.getString("id"), description, priceOffert);
				}
			}
		});
	}

	public boolean isNetworkConnected() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo == null) {
			return false;
		}
		return activeNetworkInfo.isConnected();
	}

	public class ServicesTask extends AsyncTask<String, Integer, Boolean> {
		private String secretCode;
		private String method;
		private String servicemanId;
		private String orderId;
		private String password;
		private String email;
		private String cause;
		private String msg;
		private String newPrice;

		@Override
		protected Boolean doInBackground(String... params) {
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

			switch (Integer.parseInt(params[0])) {
			case Constants.SERVICE_CLOSE_ORDER_INT:
				method = params[1];
				servicemanId = userDetailsMap.get(SessionManager.KEY_ID);
				orderId = params[2];
				secretCode = params[3];
				msg = "Usługa została zamknięta";
				break;
			case Constants.SERVICE_REGISTER_SERVICEMAN_TO_SERVICE_INT:
				method = params[1];
				servicemanId = userDetailsMap.get(SessionManager.KEY_ID);
				orderId = params[2];
				msg = "Oferta została przyjęta";
				break;

			case Constants.SERVICE_CANCEL_OFFER_INT:
				method = params[1];
				servicemanId = userDetailsMap.get(SessionManager.KEY_ID);
				orderId = params[2];
				cause = params[3];
				msg = "Oferta została anulowana";
				break;

			case Constants.SERVICE_CHANGE_ORDER_PRICE_INT:
				method = params[1];
				servicemanId = userDetailsMap.get(SessionManager.KEY_ID);
				orderId = params[2];
				cause = params[3];
				newPrice = params[4];
				msg = "Prośba o zmianę ceny została wysłana";
				break;
			case Constants.SERVICE_PUT_OFFER_INT:
				method = params[1];
				servicemanId = userDetailsMap.get(SessionManager.KEY_ID);
				orderId = params[2];
				servicemanDsc = params[3];
				servicemanPrice = params[4];
				msg = "Oferta została wysłana";
				break;

			default:
				break;
			}

			// String password = "exopass";
			// String email = "tomekbalc@gmail.com";
			int requestID = 0;

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("servicemanId", servicemanId);
			// parameters.put("type",
			// getIntent().getExtras().getInt("kategoria") + "");
			parameters.put("orderId", orderId);
			parameters.put("secretCode", secretCode);
			parameters.put("cause", cause);
			parameters.put("newPrice", newPrice);
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

			return HttpResponseAuthorizationJSONService.authResponseFromJSON(response);

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mTask = null;
			Intent i = getIntent();
			// showProgress(false);

			if (success) {
				// finish();
				dialog.dismiss();
				new AlertDialog.Builder(ShowOrderActivity.this).setMessage(msg)
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
				new AlertDialog.Builder(ShowOrderActivity.this).setMessage("Wystąpił błąd.")
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
			mTask = null;
		}
	}

	public class MyConfigurator implements ConnectionConfigurator {

		public void configure(HttpURLConnection connection) {

			// add custom HTTP header
			connection.addRequestProperty("Token", userDetailsMap.get(SessionManager.KEY_TOKEN));
		}
	}
}
