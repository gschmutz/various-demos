package com.soaringclouds.customer.repository;

import java.util.List;

import org.springframework.stereotype.Component;

import com.soaringclouds.customer.model.CustomerDO;

@Component
public class CustomerRepositoryImpl implements CustomerRepository {

	@Override
	public CustomerDO findById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CustomerDO> findCustomersByNameRegex(String searchString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CustomerDO> findCustomersBySearchString(String searchString) {
		// TODO Auto-generated method stub
		return null;
	}

}
