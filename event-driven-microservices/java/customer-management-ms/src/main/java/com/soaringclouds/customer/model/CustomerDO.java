package com.soaringclouds.customer.model;

import java.util.List;

public class CustomerDO {
    
    private String id;
    private String firstName;
    private String lastName;
    private String title; 
    private String emailAddress;
    private List<AddressDO> addresses;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public List<AddressDO> getAddresses() {
		return addresses;
	}
	public void setAddresses(List<AddressDO> addresses) {
		this.addresses = addresses;
	}
	@Override
	public String toString() {
		return "CustomerDO [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", title=" + title
				+ ", emailAddress=" + emailAddress + ", addresses=" + addresses + "]";
	}
	

}
