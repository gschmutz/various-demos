package com.soaringclouds.order.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.soaringclouds.order.model.OrderDO;

/*
 * Service Layer should be used for Transactional processes
 * 
 * Calls Repository Layers
 * 
 */
@Service
public interface OrderService {

    public List<OrderDO> findAll();
    public OrderDO findById(String id);

    public void createOrder(OrderDO product);
    public void modifyOrder(OrderDO product);
    public void removeOrder(OrderDO product);
}
