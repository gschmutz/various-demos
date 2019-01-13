package com.soaringclouds.customer.api;

import java.util.ArrayList;

import com.soaringclouds.customer.model.AddressDO;
import com.soaringclouds.customer.model.CustomerDO;

public class CustomerConverter {
	
	public static CustomerApi convert (CustomerDO customer) {
		CustomerApi value = new CustomerApi();
		
		value.setCustomerId(customer.getId());
		value.setFirstName(customer.getFirstName());
		value.setLastName(customer.getLastName());
		value.setTitle(customer.getTitle());
		value.setEmailAddress(customer.getEmailAddress());
		
		AddressApi valueAddress;
		if (customer.getAddresses() != null) {
			for (AddressDO address : customer.getAddresses()) {
				valueAddress = new AddressApi();
				valueAddress.setStreet(address.getStreet());
				valueAddress.setNumber(address.getNumber());
				valueAddress.setPostcode(address.getPostcode());
				valueAddress.setCity(address.getCity());
				valueAddress.setCountry(address.getCountry());
				
				if (value.getAddresses() == null) { value.setAddresses(new ArrayList<>()); }
				value.getAddresses().add(valueAddress);
			}
		}
		
		return value;
	}
	
	public static CustomerDO convert (CustomerApi customer) {
		CustomerDO value = new CustomerDO();
		
		value.setId(customer.getCustomerId());
		value.setFirstName(customer.getFirstName());
		value.setLastName(customer.getLastName());
		value.setTitle(customer.getTitle());
		value.setEmailAddress(customer.getEmailAddress());

		AddressDO valueAddress;
		if (customer.getAddresses() != null) { 
			for (AddressApi address : customer.getAddresses()) {
				valueAddress = new AddressDO();
				valueAddress.setStreet(address.getStreet());
				valueAddress.setNumber(address.getNumber());
				valueAddress.setPostcode(address.getPostcode());
				valueAddress.setCity(address.getCity());
				valueAddress.setCountry(address.getCountry());
				
				if (value.getAddresses() == null) { value.setAddresses(new ArrayList<>()); }
				value.getAddresses().add(valueAddress);
			}
		}
		
		return value;
	}
	
}
