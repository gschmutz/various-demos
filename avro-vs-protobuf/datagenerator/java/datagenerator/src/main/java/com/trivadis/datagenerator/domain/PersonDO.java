package com.trivadis.datagenerator.domain;

import java.util.ArrayList;
import java.util.List;

public class PersonDO {
	private Integer id;
	private String firstName;
	private String lastName;
	private String emailAddress;
	private String phoneNumber;
	private String faxNumber;
	private String title;
	private Integer dateOfBirth;
	
	private List<AddressDO> addresses;
	
	public PersonDO() {
		
	}
	
	public Integer getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getFaxNumber() {
		return faxNumber;
	}

	public String getTitle() {
		return title;
	}

	public Integer getDateOfBirth() {
		return dateOfBirth;
	}

	public List<AddressDO> getAddresses() {
		return addresses;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDateOfBirth(Integer dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public void setAddresses(List<AddressDO> addresses) {
		this.addresses = addresses;
	}

	public void addAddress(AddressDO address) {
		if (addresses == null) {
			addresses = new ArrayList<>();
		}
		this.addresses.add(address);
	}
	
}
