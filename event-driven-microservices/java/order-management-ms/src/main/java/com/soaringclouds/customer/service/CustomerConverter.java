package com.soaringclouds.customer.service;

import com.trivadis.avro.customer.v1.Customer;


public class CustomerConverter {

	public static CustomerDO convert (Customer customer) {
		CustomerDO value = new CustomerDO();
		value.setId(customer.getId().toString());
		value.setFirstName(customer.getFirstName().toString());
		value.setLastName(customer.getLastName().toString());

		return value;
	}

}
