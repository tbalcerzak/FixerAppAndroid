package pl.exorigoupos.fixerapp.data;

public class Constants {

	// public static final String SERVER_URL =
	// "http://54.229.111.255/dev/default/index/sjson";
	public static final String SERVER_URL = "http://fixer.pl/dev/jsonrpc";
	public static final String SERVER_ADDRESS = "http://fixer.pl/";

	public static final int STATUS_IN_AUCTION = 4;
	public static final int STATUS_SENT = 2;
	public static final int STATUS_FINISHED = 8;
	public static final int STATUS_ASIGNED_SERVICEMAN = 7;

	public static final String BID_MODE_1 = "Konkurs ofert";
	public static final String BID_MODE_0 = "Szybki wybór";

	public static final String SERVICE_SAVE_AVAILABILITY = "saveAvailability";
	public static final String SERVICE_AUTH_USER = "authUser";
	public static final String SERVICE_PUT_OFFER = "putOffer";
	public static final String SERVICE_CLOSE_ORDER = "closeOrder";
	public static final String SERVICE_GET_SERVICEMAN_STATUS = "getServicemanStatus";
	public static final String SERVICE_SET_SERVICEMAN_STATUS = "setServicemanStatus";
	public static final String SERVICE_GET_SERVICEMAN_SERVICES = "getServicemanServices";
	public static final String SERVICE_REGISTER_SERVICEMAN_TO_SERVICE = "registerServicemanToService";
	public static final String SERVICE_GET_FIXER_RESPONSE = "getFixerResponse";
	public static final String SERVICE_CANCEL_OFFER = "cancelOffer";
	public static final String SERVICE_CHANGE_ORDER_PRICE = "changeOrderPrice";

	public static final int SERVICE_SAVE_AVAILABILITY_INT = 1;
	public static final int SERVICE_AUTH_USER_INT = 2;
	public static final int SERVICE_PUT_OFFER_INT = 3;
	public static final int SERVICE_CLOSE_ORDER_INT = 4;
	public static final int SERVICE_GET_SERVICEMAN_STATUS_INT = 5;
	public static final int SERVICE_SET_SERVICEMAN_STATUS_INT = 6;
	public static final int SERVICE_GET_SERVICEMAN_SERVICES_INT = 7;
	public static final int SERVICE_REGISTER_SERVICEMAN_TO_SERVICE_INT = 8;
	public static final int SERVICE_GET_FIXER_RESPONSE_INT = 9;
	public static final int SERVICE_CANCEL_OFFER_INT = 10;
	public static final int SERVICE_CHANGE_ORDER_PRICE_INT = 11;

	public static final String SERVICE_MASKED_ADDRESS_INFO = "Szczegóły dostępne dla osób obsługujących zlecenie.";

}
