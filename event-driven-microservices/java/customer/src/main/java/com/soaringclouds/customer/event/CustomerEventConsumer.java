package com.soaringclouds.customer.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.soaringclouds.avro.customer.v1.Customer;

@Component
public class CustomerEventConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerEventConsumer.class);

	@KafkaListener(topics = "${kafka.topic.customer}")
	public void receive(ConsumerRecord<String, Customer> consumerRecord) {
		Customer customer = consumerRecord.value();
		// Customer product = (Customer) SpecificData.get().deepCopy(Customer.SCHEMA$,
		// consumerRecord.value());
		LOGGER.info("received payload='{}'", customer.toString());
		System.out.println("received payload= " + customer.toString());
	}
}
