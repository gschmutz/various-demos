package com.soaringclouds.customer.converter;

import java.util.ArrayList;
import java.util.List;

import com.soaringclouds.avro.customer.v1.Customer;
import com.soaringclouds.avro.customer.v1.TitleEnum;
import com.soaringclouds.customer.model.CustomerDO;

public class CustomerConverter {
	public static List<String> convertFromCS (List<CharSequence> input) {
		List<String> value = new ArrayList<String>();
		
		if (input != null) {
			for (CharSequence cs : input) {
				value.add(input.toString());
			}
		}
		return value;
	}
	
	public static List<CharSequence> convertFromString (List<String> input) {
		List<CharSequence> value = new ArrayList<CharSequence>();
		
		if (input != null) {
			for (String str : input) {
				value.add(str);
			}
		}
		return value;
	}
	
	public static Customer convert (CustomerDO customer) {
		Customer value = new Customer();
		
		value.setId(customer.getId());
		value.setFirstName(customer.getFirstName());
		value.setLastName(customer.getLastName());
		value.setTitle(TitleEnum.valueOf(customer.getTitle()));
		value.setEmail(customer.getEmailAddress());
		
		
		return value;
	}
	
	public static CustomerDO convert (Customer customer) {
		CustomerDO value = new CustomerDO();
		value.setId(customer.getId().toString());
		value.setFirstName(customer.getFirstName().toString());
		value.setLastName(customer.getLastName().toString());
		value.setTitle(customer.getTitle().toString());
		value.setEmailAddress(customer.getEmail().toString());

		return value;
	}
	

}
