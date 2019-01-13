package com.soaringclouds.order.api;

import com.soaringclouds.order.model.OrderDO;

public class OrderConverter {
	
	public static OrderApi convert (OrderDO order) {
		OrderApi value = new OrderApi();
		
		value.setOrderId(order.getId());
		value.setCustomerId(order.getCustomerId().toString());
		
		return value;
	}
	
	public static OrderDO convert (OrderApi order) {
		OrderDO value = new OrderDO();
		
		value.setId(order.getOrderId());
		value.setCustomerId(Long.valueOf(order.getCustomerId()));

		return value;
	}
	
}
