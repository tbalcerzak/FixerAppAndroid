package pl.exorigoupos.fixerapp.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.exorigoupos.fixerapp.model.Order;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class OrderJSONParsingService {

	private static final String TAG_RESULT = "result";
	private static final String TAG_RESPONSE = "response";
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_BID_MODE = "bid_mode";
	private static final String TAG_ORDER_ITEMS_PROBLEM_DSC = "problem_dsc";
	private static final String TAG_CREATED_AT = "created_at";
	private static final String TAG_COMMENTS_DSC = "comments_dsc";
	private static final String TAG_PRICE_CLIENT = "price_client";
	private static final String TAG_REALIZATION_DATE = "realization_date";
	private static final String TAG_REALIZATION_TIME_RANGE = "realization_time_range";
	private static final String TAG_ADDRESS = "Address";
	private static final String TAG_ADRESS_STREET = "street";
	private static final String TAG_ADRESS_STREET_NUMBER = "number";
	private static final String TAG_ADRESS_CITY = "city";
	private static final String TAG_ADRESS_LNG = "lng";
	private static final String TAG_ADRESS_LAT = "lat";
	private static final String TAG_ADRESS_IS_MASKED = "addres_is_masked";
	private static final String TAG_ADRESS_TELEPHONE = "tel";
	private static final String TAG_ORDER_ITEMS = "OrderItems";
	private static final String TAG_ORDER_ITEMS_PROBLEMS = "Problems";
	private static final String TAG_ORDER_ITEMS_PROBLEM_NAME = "name";
	private static final String TAG_UNIT_MODEL = "model";
	private static final String TAG_UNIT_SERIAL_NO = "serial_no";
	private static final String TAG_MANUFACTURER_NAME = "name";
	private static final String TAG_UNIT = "Unit";
	private static final String TAG_MANUFACTURER = "Manufacturer";
	private static final String TAG_TREE_ITEM = "TreeItem";
	private static final String TAG_TREE_ITEM_TITLE = "title";
	private static final String TAG_SERVICEMAN_ID = "serviceman_id";
	private static final String TAG_STATUS = "Status";
	private static final String TAG_STATUS_ID = "id";
	private static final String TAG_ASSETS = "Assets";
	private static final String TAG_ASSETS_NAME = "name";
	private static final String TAG_ASSETS_URL = "url";
	private static final String TAG_ORDER_SERVICEMANS = "OrderServicemans";
	private static final String TAG_ORDER_SERVICEMANS_PRICE = "price";
	private static final String TAG_ORDER_SERVICEMANS_DISTANCE = "distance";
	private static final String TAG_ORDER_SERVICEMANS_REALIZATION_DATE = "realization_date";
	private static final String TAG_ORDER_SERVICEMANS_REALIZATION_TIME_RANGE = "realization_time_range";
	private static final String TAG_ORDER_SERVICEMANS_OFFER_DSC = "offer_dsc";
	private static final String TAG_PROOF_OF_PURCHASE = "proof_of_purchase";
	private static final String TAG_DSC_CONDITIONS = "dsc_conditions";
	private static final String TAG_OFFER_DEDLINE = "offer_dedline";

	private List<Order> ordersList;
	private List<String> problemsList;
	private HashMap<String, String> assetsMap;

	JSONArray orders = null;

	public OrderJSONParsingService() {
		super();
		this.ordersList = new ArrayList<Order>();
		this.problemsList = new ArrayList<String>();
		this.assetsMap = new HashMap<String, String>();
	}

	public List<Order> createOrderListFromJSON(JSONRPC2Response response) {
		List<Order> zgloszeniaList2 = new ArrayList<Order>();
		JSONObject q2 = null;
		JSONParser jParser = new JSONParser();
		JSONObject json = jParser.getJSONFromResponse(response);
		Order order;
		try {
			q2 = json.getJSONObject(TAG_RESULT);
			orders = q2.getJSONArray(TAG_RESPONSE);

			for (int i = 0; i < orders.length(); i++) {
				JSONObject q = orders.getJSONObject(i);
				JSONObject address = q.getJSONObject(TAG_ADDRESS);
				JSONObject status = q.getJSONObject(TAG_STATUS);

				JSONArray orderServicemansArray = q.getJSONArray(TAG_ORDER_SERVICEMANS);

				JSONObject orderServicemans = orderServicemansArray.getJSONObject(0);

				String problem_dsc = "";
				String unit_serial_no = "";
				String unit_model = "";
				String manufacturer_name = "";
				String TreeItem_title = "";
				String assetName = "";
				String assetURL = "";

				JSONArray assetsArray = q.getJSONArray(TAG_ASSETS);
				for (int j = 0; j < assetsArray.length(); j++) {
					JSONObject item = assetsArray.getJSONObject(j);
					assetName = item.getString(TAG_ASSETS_NAME);
					// assetURL = item.getString(TAG_ASSETS_URL);
					assetURL = item.getString(TAG_ASSETS_URL).substring(item.getString(TAG_ASSETS_URL).indexOf("dev"));
					assetsMap.put(assetName, assetURL);
				}

				JSONArray orderItems = q.getJSONArray(TAG_ORDER_ITEMS);
				for (int j = 0; j < orderItems.length(); j++) {
					JSONObject item = orderItems.getJSONObject(j);

					JSONObject treeItem = item.getJSONObject(TAG_TREE_ITEM);
					TreeItem_title = treeItem.getString(TAG_TREE_ITEM_TITLE);

					problem_dsc = item.getString(TAG_ORDER_ITEMS_PROBLEM_DSC);
					JSONArray orderProblems = item.getJSONArray(TAG_ORDER_ITEMS_PROBLEMS);
					for (int k = 0; k < orderProblems.length(); k++) {
						JSONObject problem = orderProblems.getJSONObject(k);

						problemsList.add(problem.getString(TAG_ORDER_ITEMS_PROBLEM_NAME));
					}

					try {
						JSONObject unit = item.getJSONObject(TAG_UNIT);
						JSONObject manufacturer = unit.getJSONObject(TAG_MANUFACTURER);
						unit_serial_no = unit.getString(TAG_UNIT_SERIAL_NO);
						unit_model = unit.getString(TAG_UNIT_MODEL);
						manufacturer_name = manufacturer.getString(TAG_MANUFACTURER_NAME);

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				String Address_street_number = "";
				String Address_telephone = "";
				if (address.getString(TAG_ADRESS_IS_MASKED).equals("0")) {

					Address_street_number = address.getString(TAG_ADRESS_STREET_NUMBER);
					Address_telephone = address.getString(TAG_ADRESS_TELEPHONE);

				}

				String name = q.getString(TAG_NAME);
				String bid_mode = q.getString(TAG_BID_MODE);
				String id = q.getString(TAG_ID);
				String price_client = q.getString(TAG_PRICE_CLIENT);
				String created_at = q.getString(TAG_CREATED_AT);
				String realization_date = q.getString(TAG_REALIZATION_DATE);
				String realization_time_range = q.getString(TAG_REALIZATION_TIME_RANGE);
				String comments_dsc = q.getString(TAG_COMMENTS_DSC);
				String Address_street = address.getString(TAG_ADRESS_STREET);
				String Address_city = address.getString(TAG_ADRESS_CITY);
				String Address_lng = address.getString(TAG_ADRESS_LNG);
				String Address_lat = address.getString(TAG_ADRESS_LAT);
				String serviceman_id = q.getString(TAG_SERVICEMAN_ID);
				String Status_id = status.getString(TAG_STATUS_ID);
				String distanceServicemans = orderServicemans.getString(TAG_ORDER_SERVICEMANS_DISTANCE);
				String priceServicemans = orderServicemans.getString(TAG_ORDER_SERVICEMANS_PRICE);
				String dateServicemans = orderServicemans.getString(TAG_ORDER_SERVICEMANS_REALIZATION_DATE);
				String timeServicemans = orderServicemans.getString(TAG_ORDER_SERVICEMANS_REALIZATION_TIME_RANGE);
				String dscServicemans = orderServicemans.getString(TAG_ORDER_SERVICEMANS_OFFER_DSC);
				String dsc_conditions = q.getString(TAG_DSC_CONDITIONS);
				String proof_of_purchase = q.getString(TAG_PROOF_OF_PURCHASE);
				String offer_dedline = q.getString(TAG_OFFER_DEDLINE);

				order = new Order();
				order.setName(name);
				order.setBid_mode(bid_mode);
				order.setRealization_date(realization_date);
				order.setId(id);
				order.setPrice_client(price_client);
				order.setAddress_city(Address_city);
				order.setAddress_street(Address_street);
				order.setAddress_street_number(Address_street_number);
				order.setAddress_telephone(Address_telephone);
				order.setRealization_time_range(realization_time_range);
				order.setCreated_at(created_at.substring(0, created_at.length()-3));
				order.setComments_dsc(comments_dsc);
				order.setAddress_lat(Address_lat);
				order.setAddress_lng(Address_lng);
				order.setProblem_dsc(problem_dsc);
				order.setUnit_model(unit_model);
				order.setUnit_serial_no(unit_serial_no);
				order.setManufacturer_name(manufacturer_name);
				order.setTreeItem_title(TreeItem_title);
				order.setServiceman_id(serviceman_id);
				order.setStatus_id(Status_id);
				order.setOrderServicemans_distance(distanceServicemans);
				order.setOrderServicemans_price(priceServicemans);
				order.setOrderServicemans_realization_date(dateServicemans);
				order.setOrderServicemans_realization_time_range(timeServicemans);
				order.setOrderServicemans_offer_dsc(dscServicemans);
				order.setProblemsList(problemsList);
				order.setAssetsMap(assetsMap);
				order.setDsc_conditions(dsc_conditions);
				order.setProof_of_purchase(proof_of_purchase);
				order.setOffer_dedline(offer_dedline.substring(0, offer_dedline.length()-3));
				this.problemsList = new ArrayList<String>();
				this.assetsMap = new HashMap<String, String>();
				zgloszeniaList2.add(order);
			}
			return zgloszeniaList2;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return zgloszeniaList2;
	}

	public List<Order> getMarketList() {
		return ordersList;
	}
}
