package pl.exorigoupos.fixerapp.model;

public class User {

	private String id;
	private String lastName;
	private String firstName;
	private String email;
	private String working_range;
	private String working_address_street;
	private String working_address_city;
	private String working_address_number;
	private String token;

	public User() {
		// TODO Auto-generated constructor stub
	}

	public User(String id, String lastName, String firstName, String email) {
		super();
		this.id = id;
		this.lastName = lastName;
		this.firstName = firstName;
		this.email = email;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getWorking_address_street() {
		return working_address_street;
	}

	public void setWorking_address_street(String working_address_street) {
		this.working_address_street = working_address_street;
	}

	public String getWorking_address_city() {
		return working_address_city;
	}

	public void setWorking_address_city(String working_address_city) {
		this.working_address_city = working_address_city;
	}

	public String getWorking_address_number() {
		return working_address_number;
	}

	public void setWorking_address_number(String working_address_number) {
		this.working_address_number = working_address_number;
	}

	public String getWorking_range() {
		return working_range;
	}

	public void setWorking_range(String working_range) {
		this.working_range = working_range;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
