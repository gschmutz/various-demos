package com.soaringclouds.order.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderApi {

    @JsonProperty(value = "orderId", required = true)
    public String orderId;
    
    @JsonProperty(value = "customerId", required = true)
    public String customerId;
    
    @JsonProperty(value = "orderLines", required = false)
    private List<OrderLineApi> orderLines;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public List<OrderLineApi> getOrderLines() {
		return orderLines;
	}

	public void setOrderLines(List<OrderLineApi> orderLines) {
		this.orderLines = orderLines;
	}


    
}
