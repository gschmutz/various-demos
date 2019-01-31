package com.hortonworks.solution;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.hortonworks.simulator.impl.domain.transport.MobileEyeEvent;
import com.hortonworks.simulator.impl.domain.transport.Truck;

import akka.actor.UntypedActor;

public class StdOutSensorEventCollector extends AbstractSensorEventCollector {

	private Logger logger = Logger.getLogger(this.getClass());

	public StdOutSensorEventCollector() throws MqttException {

	}

	@Override
	protected void sendMessage(String topicName, MobileEyeEvent originalEvent, Object message) {
		System.out.println(message);
	}

	@Override
	protected String getTopicName(Integer eventKind, MobileEyeEvent originalEvent) {
		System.out.println(eventKind);
		return null;
	}

}
