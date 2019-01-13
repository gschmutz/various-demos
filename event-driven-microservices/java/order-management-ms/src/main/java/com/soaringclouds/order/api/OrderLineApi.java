package com.soaringclouds.order.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderLineApi {

    @JsonProperty(value = "quantity", required = true)
    public Integer quantity; 
    
    @JsonProperty(value = "productId", required = true)
    public Integer productId; 

 
    
}
