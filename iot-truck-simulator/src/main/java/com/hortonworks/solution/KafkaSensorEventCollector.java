package com.hortonworks.solution;

import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;

import com.hortonworks.simulator.impl.domain.transport.MobileEyeEvent;

public class KafkaSensorEventCollector extends AbstractSensorEventCollector {

	static final String TOPIC_TRUCK_POSITION = "truck_position";
	static final String TOPIC_TRUCK_DRIVING_INFO = "truck_driving_info";
	private Logger logger = Logger.getLogger(this.getClass());

	Producer<String, String> producer = null;

	private Producer<String, String> connect() {
		Producer<String, String> producer = null;

		Properties props = new Properties();
		props.put("bootstrap.servers", Lab.broker + ":" + Lab.port);
		props.put("acks", "all");
		props.put("retries", 0);
	    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");		

		try {
			producer = new KafkaProducer<String, String>(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return producer;
	}

	public KafkaSensorEventCollector() {

		if (producer == null) {
			producer = connect();
		}

	}

	@Override
	protected String getTopicName(Integer eventKind, MobileEyeEvent originalEvent) {
		String topicName = null;
		if (eventKind.equals(MobileEyeEvent.EVENT_KIND_BEHAVIOUR_AND_POSITION)) {
			topicName = TOPIC_TRUCK_POSITION;
		} else if (eventKind.equals(MobileEyeEvent.EVENT_KIND_POSITION)) {
			topicName = TOPIC_TRUCK_POSITION;
		} else if (eventKind.equals(MobileEyeEvent.EVENT_KIND_BEHAVIOUR)) {
			topicName = TOPIC_TRUCK_DRIVING_INFO;
		}
		return topicName;	
	}

	@Override
	protected void sendMessage(String topicName, MobileEyeEvent originalEvent, Object message) {
		String truckId = String.valueOf(originalEvent.getTruck().getTruckId());
		
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(topicName, truckId, (String)message);

		if (producer != null) {
			try {
				Future<RecordMetadata> future = producer.send(record);
				RecordMetadata metadata = future.get();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
