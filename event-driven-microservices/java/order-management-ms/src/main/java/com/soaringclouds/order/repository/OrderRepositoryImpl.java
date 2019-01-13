package com.soaringclouds.order.repository;

import java.util.List;
import org.springframework.stereotype.Component;
import com.soaringclouds.order.model.OrderDO;

@Component
public class OrderRepositoryImpl implements OrderRepository {

	@Override
	public OrderDO findById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderDO> findOrdersByNameRegex(String searchString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderDO> findOrdersBySearchString(String searchString) {
		// TODO Auto-generated method stub
		return null;
	}

}
