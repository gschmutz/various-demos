package com.soaringclouds.order.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.trivadis.avro.order.v1.Order;

@Component
public class OrderEventProducer {
	@Autowired
	private KafkaTemplate<String, Order> kafkaTemplate;
	
	@Value("${kafka.topic.order}")
	String kafkaTopic;
	
	public void produce(Order order) {
		kafkaTemplate.send(kafkaTopic, order.getOrderId().toString(), order);
	}
}
