package com.soaringclouds.customer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.soaringclouds.customer.converter.CustomerConverter;
import com.soaringclouds.customer.event.CustomerEventProducer;
import com.soaringclouds.customer.model.CustomerDO;
import com.soaringclouds.customer.repository.CustomerRepository;

@Component
public class CustomerServiceImpl implements CustomerService {
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private CustomerEventProducer customerEventProducer;
	
	public List<CustomerDO> findAll() {
		return null;
//		return customerRepository.findAll();
	}
	
	public CustomerDO findById(String id) {
		return customerRepository.findById(id);
	}
	
	public List<CustomerDO> findCustomersByName(String searchString) {
		return customerRepository.findCustomersByNameRegex(searchString);
	}
		
	@Override
	public void createCustomer(CustomerDO customer) {
		//customer.setId(UUID.randomUUID());
//		customerRepository.save(customer);
		
		com.soaringclouds.avro.customer.v1.Customer avro = CustomerConverter.convert(customer);
		customerEventProducer.produce(avro);
	}
	
	@Override
	public void modifyCustomer(CustomerDO customer) {
//		customerRepository.save(customer);

		com.soaringclouds.avro.customer.v1.Customer avro = CustomerConverter.convert(customer);
		customerEventProducer.produce(avro);
	}	
	
	@Override
	public void removeCustomer(CustomerDO customer) {
		//customerRepository.remove(customer);

		//com.soaringclouds.avro.customer.v1.Customer avro = CustomerConverter.convert(customer);
		//customerEventProducer.produce(avro);
	}
	
	 
}
