package com.soaringclouds.customer.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddressApi {

    @JsonProperty(value = "street", required = true)
    public String street; 
    
    @JsonProperty(value = "number", required = true)
    public String number; 
    
    @JsonProperty(value = "city", required = true)
    public String city;

    @JsonProperty(value = "postcode", required = true)
    public String postcode;

    @JsonProperty(value = "country", required = true)
    public String country;

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

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String toString() {
		return "AddressApi [street=" + street + ", number=" + number + ", city=" + city + ", postcode=" + postcode
				+ ", country=" + country + "]";
	}
    
}
