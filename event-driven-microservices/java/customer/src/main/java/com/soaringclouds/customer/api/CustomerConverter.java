package com.soaringclouds.customer.api;

import com.soaringclouds.customer.model.CustomerDO;

public class CustomerConverter {
	
	public static CustomerApi convert (CustomerDO customer) {
		CustomerApi value = new CustomerApi();
		
		value.setCustomerId(customer.getId());
		value.setFirstName(customer.getFirstName());
		value.setLastName(customer.getLastName());
		value.setTitle(customer.getTitle());
		value.setEmailAddress(customer.getEmailAddress());
		
		return value;
	}
	
	public static CustomerDO convert (CustomerApi customer) {
		CustomerDO value = new CustomerDO();
		
		value.setId(customer.getCustomerId());
		value.setFirstName(customer.getFirstName());
		value.setLastName(customer.getLastName());
		value.setTitle(customer.getTitle());
		value.setEmailAddress(customer.getEmailAddress());

		return value;
	}
	
}
