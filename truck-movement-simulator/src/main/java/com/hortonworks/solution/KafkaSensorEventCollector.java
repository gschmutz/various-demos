package com.hortonworks.solution;

import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;

import com.hortonworks.simulator.impl.domain.transport.MobileEyeEvent;

import akka.actor.UntypedActor;

public class KafkaSensorEventCollector extends UntypedActor {

	private static final String TOPIC_TRUCK_POSITION = "truck_position";
	private static final String TOPIC_TRUCK_DRIVING_INFO = "truck_driving_info";
	private Logger logger = Logger.getLogger(this.getClass());

	private Producer<String, String> producer = null;

	private Producer<String, String> connect() {
		Producer<String, String> producer = null;

		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
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

	private void sendMessage(ProducerRecord<String, String> record) {
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
	
	@Override
	public void onReceive(Object event) throws Exception {
		MobileEyeEvent mee = (MobileEyeEvent) event;
		String eventToPass = null;

		String truckId = String.valueOf(mee.getTruck().getTruckId());

		ProducerRecord<String, String> record = null;
		
		if (Lab.mode.equals(Lab.COMBINE)) {
			if (Lab.format.equals(Lab.JSON)) {
		        eventToPass = mee.toJSON(MobileEyeEvent.EVENT_KIND_BEHAVIOUR_AND_POSITION, Lab.timeResolution);
		    } else if (Lab.format.equals(Lab.CSV)) {
		        eventToPass = mee.toCSV(MobileEyeEvent.EVENT_KIND_BEHAVIOUR_AND_POSITION, Lab.timeResolution);
		    }
			record = new ProducerRecord<String, String>(TOPIC_TRUCK_POSITION, truckId, eventToPass);
			sendMessage(record);
		} else if (Lab.mode.equals(Lab.SPLIT)) {
			if (Lab.format.equals(Lab.JSON)) {
		        eventToPass = mee.toJSON(MobileEyeEvent.EVENT_KIND_BEHAVIOUR, Lab.timeResolution);
		    } else if (Lab.format.equals(Lab.CSV)) {
		        eventToPass = mee.toCSV(MobileEyeEvent.EVENT_KIND_BEHAVIOUR, Lab.timeResolution);
		    }
			
			Thread.sleep((long)(Math.random() * 200));

			record = new ProducerRecord<String, String>(TOPIC_TRUCK_DRIVING_INFO, truckId, eventToPass);
			sendMessage(record);

			if (Lab.format.equals(Lab.JSON)) {
		        eventToPass = mee.toJSON(MobileEyeEvent.EVENT_KIND_POSITION, Lab.timeResolution);
		    } else if (Lab.format.equals(Lab.CSV)) {
		        eventToPass = mee.toCSV(MobileEyeEvent.EVENT_KIND_POSITION, Lab.timeResolution);
		    }

			record = new ProducerRecord<String, String>(TOPIC_TRUCK_POSITION, truckId, eventToPass);
			sendMessage(record);
		}
		


	}
}
