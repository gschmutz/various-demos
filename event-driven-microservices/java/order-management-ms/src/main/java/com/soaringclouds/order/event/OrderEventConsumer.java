package com.soaringclouds.order.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.trivadis.avro.order.v1.Order;

@Component
public class OrderEventConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventConsumer.class);

	//@KafkaListener(topics = "${kafka.topic.order}", containerFactory="orderConsumerFactory")
	public void receive(ConsumerRecord<String, Order> consumerRecord) {
		Order order = consumerRecord.value();
		// Order product = (Order) SpecificData.get().deepCopy(Order.SCHEMA$,
		// consumerRecord.value());
		LOGGER.info("received payload='{}'", order.toString());
		System.out.println("received payload= " + order.toString());
	}
}
