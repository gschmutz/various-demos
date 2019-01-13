package com.soaringclouds.customer.service;

import org.springframework.stereotype.Service;

/*
 * Service Layer should be used for Transactional processes
 * 
 * Calls Repository Layers
 * 
 */

@Service
public interface CustomerService {

    public void modifyCustomer(CustomerDO customer);
}
