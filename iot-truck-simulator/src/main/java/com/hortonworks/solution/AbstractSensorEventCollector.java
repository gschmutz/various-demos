package com.hortonworks.solution;

import org.apache.kafka.clients.producer.ProducerRecord;

import com.hortonworks.simulator.impl.domain.transport.MobileEyeEvent;

import akka.actor.UntypedActor;

public abstract class AbstractSensorEventCollector extends UntypedActor {

	public AbstractSensorEventCollector() {
		super();
	}

	abstract protected void sendMessage(String topicName, MobileEyeEvent originalEvent, Object message);
	abstract protected String getTopicName(Integer eventKind, MobileEyeEvent originalEvent);
	
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
			sendMessage(getTopicName(MobileEyeEvent.EVENT_KIND_BEHAVIOUR_AND_POSITION, mee), mee, eventToPass);
		} else if (Lab.mode.equals(Lab.SPLIT)) {
			if (Lab.format.equals(Lab.JSON)) {
		        eventToPass = mee.toJSON(MobileEyeEvent.EVENT_KIND_BEHAVIOUR, Lab.timeResolution);
		    } else if (Lab.format.equals(Lab.CSV)) {
		        eventToPass = mee.toCSV(MobileEyeEvent.EVENT_KIND_BEHAVIOUR, Lab.timeResolution);
		    }
			
			Thread.sleep((long)(Math.random() * 100));
	
			sendMessage(getTopicName(MobileEyeEvent.EVENT_KIND_BEHAVIOUR, mee), mee, eventToPass);
	
			if (Lab.format.equals(Lab.JSON)) {
		        eventToPass = mee.toJSON(MobileEyeEvent.EVENT_KIND_POSITION, Lab.timeResolution);
		    } else if (Lab.format.equals(Lab.CSV)) {
		        eventToPass = mee.toCSV(MobileEyeEvent.EVENT_KIND_POSITION, Lab.timeResolution);
		    }
	
			sendMessage(getTopicName(MobileEyeEvent.EVENT_KIND_POSITION, mee), mee, eventToPass);
		}
		
	
	
	}

}