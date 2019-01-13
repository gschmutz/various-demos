package com.soaringclouds.order.model;

import java.util.List;

public class OrderDO {
    
    private String id;
    private String status;
    private Double totalPrice;
    private Integer discount;
    private String currency;
    private Long customerId;
    private List<OrderLineDO> orderLines;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Double getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}
	
	public Integer getDiscount() {
		return discount;
	}
	public void setDiscount(Integer discount) {
		this.discount = discount;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	public List<OrderLineDO> getOrderLines() {
		return orderLines;
	}
	public void setOrderLines(List<OrderLineDO> orderLines) {
		this.orderLines = orderLines;
	}


}
