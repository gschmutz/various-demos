package com.soaringclouds.order.converter;

import java.util.ArrayList;
import java.util.List;

import com.soaringclouds.order.model.OrderDO;
import com.trivadis.avro.order.v1.CurrencyEnum;
import com.trivadis.avro.order.v1.Order;
import com.trivadis.avro.order.v1.OrderStatusEnum;

public class OrderConverter {
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
	
	public static Order convert (OrderDO order) {
		Order value = new Order();
		
		value.setOrderId(order.getId());
		value.setCurrency(CurrencyEnum.valueOf(order.getCurrency()));
		value.setStatus(OrderStatusEnum.valueOf(order.getStatus()));
		value.setDiscount(order.getDiscount());
		
		return value;
	}
	
	public static OrderDO convert (Order order) {
		OrderDO value = new OrderDO();
		
		value.setId(order.getOrderId().toString());
		value.setCurrency(order.getCurrency().toString());
		value.setStatus(order.getStatus().toString());
		value.setDiscount(order.getDiscount());

		return value;
	}
	

}
