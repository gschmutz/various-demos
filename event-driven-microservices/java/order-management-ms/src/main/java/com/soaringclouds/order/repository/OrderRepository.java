package com.soaringclouds.order.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.soaringclouds.order.model.OrderDO;

/*
 * Repository Layer is responsible for retrival of data
 */
@Repository
public interface OrderRepository  {
	
	  OrderDO findById(String id);
	
	  
	  /*
	   * db.products.find( { 'productName': /mens/i } );
	   * @Query("{ 'productName': /?0/i }")
	   * 
	   */
//	  @Query("{ 'firstName':{$regex:?0,$options:'i'} }") 
	  List<OrderDO> findOrdersByNameRegex(String searchString);

	  /**
	   * find all products by search string. you need the following index on MongoDB: db.products.createIndex({ "$**": "text" },{ name: "TextIndex" })
	   * @param searchString
	   * @return
	   */
//	  @Query("{ $text: { $search: ?0  } }")
	  List<OrderDO> findOrdersBySearchString(String searchString);

}
