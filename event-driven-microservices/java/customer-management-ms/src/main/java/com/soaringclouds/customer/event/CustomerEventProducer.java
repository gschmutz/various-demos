package com.soaringclouds.customer.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.trivadis.avro.customer.v1.Customer;

@Component
public class CustomerEventProducer {
	@Autowired
	private KafkaTemplate<String, Customer> kafkaTemplate;
	
	@Value("${kafka.topic.customer}")
	String kafkaTopic;
	
	public void produce(Customer customer) {
		kafkaTemplate.send(kafkaTopic, customer.getId().toString(), customer);
	}
}
