package com.soaringclouds.customer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.soaringclouds.order.event.CustomerEventConsumer;

@Component
public class CustomerServiceImpl implements CustomerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceImpl.class);
	

	@Override
	public void modifyCustomer(CustomerDO customer) {
		LOGGER.info("storing customer locally'{}'", customer.toString());

	}	
	

	 
}
