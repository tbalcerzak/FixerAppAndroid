package pl.exorigoupos.fixerapp.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class HttpResponseAuthorizationJSONService {

	private static final String TAG_RESULT = "result";
	private static final String TAG_RESPONSE = "response";
	private static final String TAG_ERROR = "error";
	private static final String TAG_FALSE = "false";

	public HttpResponseAuthorizationJSONService() {
		super();
	}

	public static boolean authResponseFromJSON(JSONRPC2Response response) {
		JSONParser jParser = new JSONParser();
		JSONObject json = jParser.getJSONFromResponse(response);
		try {
			JSONObject result = json.getJSONObject(TAG_RESULT);

			String responseString = result.getString(TAG_RESPONSE);
			if (responseString.contains(TAG_ERROR) || responseString.contains(TAG_FALSE)) {
				return false;
			} else {
				return true;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
}
