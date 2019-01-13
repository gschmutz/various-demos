package com.soaringclouds.customer.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.soaringclouds.customer.converter.CustomerConverter;
import com.soaringclouds.customer.model.CustomerDO;
import com.soaringclouds.customer.repository.CustomerRepository;
import com.trivadis.avro.customer.v1.Customer;

@Component
public class CustomerEventConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerEventConsumer.class);
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@KafkaListener(topics = "${kafka.topic.customer}")
	public void receive(ConsumerRecord<String, Customer> consumerRecord) {
		Customer customer = consumerRecord.value();
		LOGGER.info("received payload='{}'", customer.toString());
		
		/*
		 * Persist customer
		 */
		CustomerDO customerDO = CustomerConverter.convert(customer);
		//customer.setId(UUID.randomUUID());
		//customerRepository.save(customerDO);
		

	}
}
