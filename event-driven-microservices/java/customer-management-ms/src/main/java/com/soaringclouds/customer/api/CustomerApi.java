package com.soaringclouds.customer.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerApi {

    @JsonProperty(value = "customerId", required = true)
    public String customerId;
    
    @JsonProperty(value = "firstName", required = true)
    public String firstName; 
    
    @JsonProperty(value = "lastName", required = true)
    public String lastName; 
    
    @JsonProperty(value = "title", required = true)
    public String title;

    @JsonProperty(value = "emailAddress", required = true)
    public String emailAddress;

    @JsonProperty(value = "addresses", required = false)
    private List<AddressApi> addresses;

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public List<AddressApi> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<AddressApi> addresses) {
		this.addresses = addresses;
	}

	@Override
	public String toString() {
		return "CustomerApi [customerId=" + customerId + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", title=" + title + ", emailAddress=" + emailAddress + ", addresses=" + addresses + "]";
	}
	
	
    
}
