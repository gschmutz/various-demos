package com.soaringclouds.customer;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import com.trivadis.avro.customer.v1.Customer;

public class TestKafkaAvro {

	private Producer<String, Customer> connect() {
		Producer<String, Customer> producer = null;

		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
		props.put("schema.registry.url", "http://localhost:8081");

		try {
			producer = new KafkaProducer<String, Customer>(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return producer;
	}
	
}
