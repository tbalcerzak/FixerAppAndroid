package pl.exorigoupos.fixerapp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;

public class Order {

	private String id;
	private String name;
	private String bid_mode;
	private String OrderItems_problem_dsc;
	private String created_at;
	private String comments_dsc;
	private String problem_dsc;
	private String Address_lng;
	private String Address_lat;
	private String Address_street;
	private String Address_street_number;
	private String Address_city;
	private String Address_telephone;
	private String realization_date;
	private String realization_time_range;
	private String price_client;
	private String Unit_serial_no;
	private String Unit_model;
	private String Manufacturer_name;
	private String TreeItem_title;
	private String serviceman_id;
	private String Status_id;
	private String OrderServicemans_distance;
	private String OrderServicemans_price;
	private String OrderServicemans_realization_date;
	private String OrderServicemans_realization_time_range;
	private String OrderServicemans_offer_dsc;
	private List<String> problemsList;
	private HashMap<String, String> assetsMap;

	private String dsc_conditions;
	private String proof_of_purchase;
	private String offer_dedline;

	public Order() {
		// TODO Auto-generated constructor stub
	}

	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putString("id", id);
		b.putString("name", name);
		b.putString("bid_mode", bid_mode);
		b.putString("problem_dsc", OrderItems_problem_dsc);
		b.putString("created_at", created_at);
		b.putString("comments_dsc", comments_dsc);
		b.putString("Address_lng", Address_lng);
		b.putString("Address_lat", Address_lat);
		b.putString("Address_street", Address_street);
		b.putString("Address_street_number", Address_street_number);
		b.putString("Address_city", Address_city);
		b.putString("Address_telephone", Address_telephone);
		b.putString("realization_date", realization_date);
		b.putString("realization_time_range", realization_time_range);
		b.putString("price_client", price_client);
		b.putString("Unit_serial_no", Unit_serial_no);
		b.putString("Unit_model", Unit_model);
		b.putString("Manufacturer_name", Manufacturer_name);
		b.putString("TreeItem_title", TreeItem_title);
		b.putString("problem_dsc", problem_dsc);
		b.putString("serviceman_id", serviceman_id);
		b.putString("Status_id", Status_id);
		b.putString("OrderServicemans_distance", OrderServicemans_distance);
		b.putString("OrderServicemans_price", OrderServicemans_price);
		b.putString("OrderServicemans_realization_date", OrderServicemans_realization_date);
		b.putString("OrderServicemans_realization_time_range", OrderServicemans_realization_time_range);
		b.putString("OrderServicemans_offer_dsc", OrderServicemans_offer_dsc);
		b.putStringArrayList("problemsList", (ArrayList<String>) problemsList);
		b.putSerializable("assetsMap", assetsMap);
		b.putString("offer_dedline", offer_dedline);
		b.putString("dsc_conditions", dsc_conditions);
		b.putString("proof_of_purchase", proof_of_purchase);

		return b;
	}

	/**
	 * @return the dsc_conditions
	 */
	public String getDsc_conditions() {
		return dsc_conditions;
	}

	/**
	 * @param dsc_conditions
	 *            the dsc_conditions to set
	 */
	public void setDsc_conditions(String dsc_conditions) {
		this.dsc_conditions = dsc_conditions;
	}

	/**
	 * @return the proof_of_purchase
	 */
	public String getProof_of_purchase() {
		return proof_of_purchase;
	}

	/**
	 * @param proof_of_purchase
	 *            the proof_of_purchase to set
	 */
	public void setProof_of_purchase(String proof_of_purchase) {
		this.proof_of_purchase = proof_of_purchase;
	}

	/**
	 * @return the offer_dedline
	 */
	public String getOffer_dedline() {
		return offer_dedline;
	}

	/**
	 * @param offer_dedline
	 *            the offer_dedline to set
	 */
	public void setOffer_dedline(String offer_dedline) {
		this.offer_dedline = offer_dedline;
	}

	/**
	 * @return the assetsMap
	 */
	public HashMap<String, String> getAssetsMap() {
		return assetsMap;
	}

	/**
	 * @param assetsMap
	 *            the assetsMap to set
	 */
	public void setAssetsMap(HashMap<String, String> assetsMap) {
		this.assetsMap = assetsMap;
	}

	public List<String> getProblemsList() {
		return problemsList;
	}

	public void setProblemsList(List<String> problemsList) {
		this.problemsList = problemsList;
	}

	public String getAddress_street_number() {
		return Address_street_number;
	}

	public void setAddress_street_number(String address_street_number) {
		Address_street_number = address_street_number;
	}

	public String getAddress_telephone() {
		return Address_telephone;
	}

	public void setAddress_telephone(String address_telephone) {
		Address_telephone = address_telephone;
	}

	public String getOrderServicemans_offer_dsc() {
		return OrderServicemans_offer_dsc;
	}

	public void setOrderServicemans_offer_dsc(String orderServicemans_offer_dsc) {
		OrderServicemans_offer_dsc = orderServicemans_offer_dsc;
	}

	public String getOrderServicemans_distance() {
		return OrderServicemans_distance;
	}

	public void setOrderServicemans_distance(String orderServicemans_distance) {
		OrderServicemans_distance = orderServicemans_distance;
	}

	public String getOrderServicemans_price() {
		return OrderServicemans_price;
	}

	public void setOrderServicemans_price(String orderServicemans_price) {
		OrderServicemans_price = orderServicemans_price;
	}

	public String getOrderServicemans_realization_date() {
		return OrderServicemans_realization_date;
	}

	public void setOrderServicemans_realization_date(String orderServicemans_realization_date) {
		OrderServicemans_realization_date = orderServicemans_realization_date;
	}

	public String getOrderServicemans_realization_time_range() {
		return OrderServicemans_realization_time_range;
	}

	public void setOrderServicemans_realization_time_range(String orderServicemans_realization_time_time_range) {
		OrderServicemans_realization_time_range = orderServicemans_realization_time_time_range;
	}

	public String getStatus_id() {
		return Status_id;
	}

	public void setStatus_id(String status_id) {
		Status_id = status_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceman_id() {
		return serviceman_id;
	}

	public void setServiceman_id(String serviceman_id) {
		this.serviceman_id = serviceman_id;
	}

	public String getProblem_dsc() {
		return problem_dsc;
	}

	public void setProblem_dsc(String problem_dsc) {
		this.problem_dsc = problem_dsc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBid_mode() {
		return bid_mode;
	}

	public void setBid_mode(String bid_mode) {
		this.bid_mode = bid_mode;
	}

	public String getOrderItems_problem_dsc() {
		return OrderItems_problem_dsc;
	}

	public void setOrderItems_problem_dsc(String orderItems_problem_dsc) {
		OrderItems_problem_dsc = orderItems_problem_dsc;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getComments_dsc() {
		return comments_dsc;
	}

	public void setComments_dsc(String comments_dsc) {
		this.comments_dsc = comments_dsc;
	}

	public String getAddress_lng() {
		return Address_lng;
	}

	public void setAddress_lng(String address_lng) {
		Address_lng = address_lng;
	}

	public String getAddress_lat() {
		return Address_lat;
	}

	public void setAddress_lat(String address_lat) {
		Address_lat = address_lat;
	}

	public String getAddress_street() {
		return Address_street;
	}

	public void setAddress_street(String address_street) {
		Address_street = address_street;
	}

	public String getAddress_city() {
		return Address_city;
	}

	public void setAddress_city(String address_city) {
		Address_city = address_city;
	}

	public String getRealization_date() {
		return realization_date;
	}

	public void setRealization_date(String realization_date) {
		this.realization_date = realization_date;
	}

	public String getRealization_time_range() {
		return realization_time_range;
	}

	public void setRealization_time_range(String realization_time_range) {
		this.realization_time_range = realization_time_range;
	}

	public String getPrice_client() {
		return price_client;
	}

	public void setPrice_client(String price_client) {
		this.price_client = price_client;
	}

	public String getUnit_serial_no() {
		return Unit_serial_no;
	}

	public void setUnit_serial_no(String unit_serial_no) {
		Unit_serial_no = unit_serial_no;
	}

	public String getUnit_model() {
		return Unit_model;
	}

	public void setUnit_model(String unit_model) {
		Unit_model = unit_model;
	}

	public String getManufacturer_name() {
		return Manufacturer_name;
	}

	public void setManufacturer_name(String manufacturer_name) {
		Manufacturer_name = manufacturer_name;
	}

	public String getTreeItem_title() {
		return TreeItem_title;
	}

	public void setTreeItem_title(String treeItem_title) {
		TreeItem_title = treeItem_title;
	}
}
