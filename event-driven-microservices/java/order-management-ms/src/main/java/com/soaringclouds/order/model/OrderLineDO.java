package com.soaringclouds.order.model;

public class OrderLineDO {
    
    private Long qty;
    private Long productId;
    
	public Long getQty() {
		return qty;
	}
	public void setQty(Long qty) {
		this.qty = qty;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}

}
