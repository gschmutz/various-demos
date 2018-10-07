package com.soaringclouds.customer.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.soaringclouds.customer.model.CustomerDO;


/*
 * Repository Layer is responsible for retrival of data
 */
@Repository
public interface CustomerRepository  {
	
	  CustomerDO findById(String id);
	
	  
	  /*
	   * db.products.find( { 'productName': /mens/i } );
	   * @Query("{ 'productName': /?0/i }")
	   * 
	   */
//	  @Query("{ 'firstName':{$regex:?0,$options:'i'} }") 
	  List<CustomerDO> findCustomersByNameRegex(String searchString);

	  /**
	   * find all products by search string. you need the following index on MongoDB: db.products.createIndex({ "$**": "text" },{ name: "TextIndex" })
	   * @param searchString
	   * @return
	   */
//	  @Query("{ $text: { $search: ?0  } }")
	  List<CustomerDO> findCustomersBySearchString(String searchString);

}
