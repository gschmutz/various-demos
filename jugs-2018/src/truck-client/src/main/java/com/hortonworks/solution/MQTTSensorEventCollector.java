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

public class MQTTSensorEventCollector extends UntypedActor {

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
	  return TOPIC + "/" + truck.getDriver().getDriverId() + "/" + "position";
  }
  
  @Override
  public void onReceive(Object event) throws Exception {
    MobileEyeEvent mee = (MobileEyeEvent) event;
    String eventToPass = null;
    if (Lab.format.equals(Lab.JSON)) {
        eventToPass = mee.toJSON();
    } else if (Lab.format.equals(Lab.CSV)) {
        eventToPass = mee.toCSV();
//    } else if (Lab.format.equals(Lab.AVRO)) {
//        eventToPass = mee.toAVRO();
    }
    String driverId = String.valueOf(mee.getTruck().getDriver().getDriverId());
    
    //System.out.println(mee.getTruck().getDriver().getDriverId() & 2);

    //logger.debug("Creating event[" + eventToPass + "] for driver[" + driverId + "] in truck [" + mee.getTruck() + "]");
    
    try {
        System.out.println("Publishing message to MQTT: "+eventToPass);
        MqttMessage message = new MqttMessage(eventToPass.getBytes());
        message.setQos(qos);
        sampleClient.publish(topicName(mee.getTruck()), message);
        //sampleClient.disconnect();
    	
    } catch (MqttException e) {
      logger.error("Error sending event[" + eventToPass + "] to MQTT topic", e);
    }
  }
}
