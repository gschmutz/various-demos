package com.soaringclouds.customer.model;

public class AddressDO {
    
    private String street;
    private String number;
    private String postcode; 
    private String city;
    private String country;
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
	@Override
	public String toString() {
		return "AddressDO [street=" + street + ", number=" + number + ", postcode=" + postcode + ", city=" + city
				+ ", country=" + country + "]";
	}


}
