package com.soaringclouds.customer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.soaringclouds.customer.model.CustomerDO;

/*
 * Service Layer should be used for Transactional processes
 * 
 * Calls Repository Layers
 * 
 */
@Service
public interface CustomerService {

    public List<CustomerDO> findAll();
    public CustomerDO findById(String id);
    public List<CustomerDO> findCustomersByName(String searchString);	

    public void createCustomer(CustomerDO product);
    public void modifyCustomer(CustomerDO product);
    public void removeCustomer(CustomerDO product);
}
