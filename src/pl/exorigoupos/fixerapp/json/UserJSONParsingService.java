package pl.exorigoupos.fixerapp.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.exorigoupos.fixerapp.FixerApplication;
import pl.exorigoupos.fixerapp.model.User;
import android.content.SharedPreferences;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class UserJSONParsingService {

	private static final String TAG_RESULT = "result";
	private static final String TAG_RESPONSE = "response";
	private static final String TAG_ERROR = "error";
	private static final String TAG_WORKING_RANGE = "working_range";
	private static final String TAG_SERVICEMAN = "Serviceman";
	private static final String TAG_ID = "id";
	private static final String TAG_TOKEN = "token";
	private static final String TAG_WORKING_ADDRESS = "working_address";
	private static final String TAG_WORKING_ADDRESS_CITY = "city";
	private static final String TAG_WORKING_ADDRESS_STREET = "street";
	private static final String TAG_WORKING_ADDRESS_NUMBER = "number";

	private List<User> userList;

	JSONArray zgloszenia = null;

	public UserJSONParsingService() {
		super();
		this.userList = new ArrayList<User>();
	}

	public Boolean authAndCreateUserFromJSON(JSONRPC2Response response) {
		List<User> userList2 = new ArrayList<User>();

		JSONParser jParser = new JSONParser();
		JSONObject json = jParser.getJSONFromResponse(response);
		try {
			JSONObject result = json.getJSONObject(TAG_RESULT);
			// JSONObject respo = result.getJSONObject(TAG_RESPONSE);

			String responseString = result.getString(TAG_RESPONSE);
			String token = result.getString(TAG_TOKEN);
			// JSONObject error = respo.getJSONObject(TAG_ERROR);

			if (responseString.contains(TAG_ERROR)) {
				return false;
			} else {
				JSONObject responseMan = result.getJSONObject(TAG_RESPONSE);
				JSONObject serviceMan = responseMan.getJSONObject(TAG_SERVICEMAN);
				String id = serviceMan.getString(TAG_ID);
				String working_range = serviceMan.getString(TAG_WORKING_RANGE);

				JSONObject workingAddress = serviceMan.getJSONObject(TAG_WORKING_ADDRESS);
				String working_address_city = workingAddress.getString(TAG_WORKING_ADDRESS_CITY);
				String working_address_street = workingAddress.getString(TAG_WORKING_ADDRESS_STREET);
				String working_address_number = workingAddress.getString(TAG_WORKING_ADDRESS_NUMBER);
				FixerApplication.user.setId(id);
				FixerApplication.user.setWorking_range(working_range);
				FixerApplication.user.setWorking_address_city(working_address_city);
				FixerApplication.user.setWorking_address_street(working_address_street);
				FixerApplication.user.setWorking_address_number(working_address_number);
				FixerApplication.user.setToken(token);

				return true;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	private SharedPreferences getSharedPreferences(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<User> getUserList() {
		return userList;
	}
}
