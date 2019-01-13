package com.hortonworks.solution;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.hortonworks.simulator.impl.domain.transport.MobileEyeEvent;
import com.hortonworks.simulator.impl.domain.transport.Truck;

public class MQTTSensorEventCollector extends AbstractSensorEventCollector {

	private MqttClient sampleClient = null;
	private static final String TOPIC = "truck";
	private int qos = 2;
	private String broker = "tcp://localhost:" + ((Lab.port == null) ? "1883" : Lab.port);
	private String clientId = "TrucksProducer";

	private Logger logger = Logger.getLogger(this.getClass());

	public MQTTSensorEventCollector() throws MqttException {
		sampleClient = new MqttClient(broker, clientId);
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		System.out.println("Connecting to MQTT broker: " + broker);
		sampleClient.connect(connOpts);

	}

	public String topicName(Truck truck) {
		return TOPIC + "/" + truck.getTruckId() + "/" + "position";
	}

	@Override
	protected String getTopicName(Integer eventKind, MobileEyeEvent originalEvent) {
		String topicName = null;
		Integer truckId = originalEvent.getTruck().getTruckId();
		if (eventKind.equals(MobileEyeEvent.EVENT_KIND_BEHAVIOUR_AND_POSITION)) {
			topicName = TOPIC + "/" + truckId + "/" + "position";;
		} else if (eventKind.equals(MobileEyeEvent.EVENT_KIND_POSITION)) {
			topicName = TOPIC + "/" + truckId + "/" + "position";;
		} else if (eventKind.equals(MobileEyeEvent.EVENT_KIND_BEHAVIOUR)) {
			topicName = TOPIC + "/" + truckId + "/" + "driving-info";;
		}
		return topicName;	
	}
	
	@Override
	protected void sendMessage(String topicName, MobileEyeEvent originalEvent, Object message) {
		String eventToPass = (String)message;
		if (sampleClient != null) {
//			System.out.println("Publishing message to MQTT: " + eventToPass);
			MqttMessage mqttMessage = new MqttMessage(eventToPass.getBytes());
			mqttMessage.setQos(qos);
			try {
				sampleClient.publish(topicName, mqttMessage);
			} catch (MqttException e) {
				logger.error("Error sending event[" + eventToPass + "] to MQTT topic", e);
			}
		}
	}
	

}
