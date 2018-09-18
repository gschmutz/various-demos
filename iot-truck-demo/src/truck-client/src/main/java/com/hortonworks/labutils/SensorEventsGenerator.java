package com.hortonworks.labutils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.hortonworks.simulator.impl.domain.transport.TruckConfiguration;
import com.hortonworks.simulator.impl.messages.StartSimulation;
import com.hortonworks.simulator.listeners.SimulatorListener;
import com.hortonworks.simulator.masters.SimulationMaster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Random;


public class SensorEventsGenerator {

  public void generateTruckEventsStream(final SensorEventsParam params) {
    try {
      final Class eventEmitterClass = Class.forName(params.getEventEmitterClassName());
      final Class eventCollectorClass = Class.forName(params.getEventCollectorClassName());
      Config config = ConfigFactory.load();
      TruckConfiguration.initialize(params.getRouteDirectory());
      int emitters = TruckConfiguration.freeRoutePool.size();

      Thread.sleep(5000);
      System.out.println("Number of Emitters is ....." + emitters);

      ActorSystem system = ActorSystem.create("EventSimulator", config, getClass().getClassLoader());
      final ActorRef listener = system.actorOf(
          Props.create(SimulatorListener.class), "listener");
      final ActorRef eventCollector = system.actorOf(
          Props.create(eventCollectorClass), "eventCollector");
      final int numberOfEmitters = emitters;
      System.out.println(eventCollector.path());
      final long demoId = new Random().nextLong();
      final Props props = Props.create(SimulationMaster.class, numberOfEmitters,
          eventEmitterClass, listener, params.getNumberOfEvents(), demoId, params.getDelayBetweenEvents());
      final ActorRef master = system.actorOf(props);
      master.tell(new StartSimulation(), master);
    } catch (Exception e) {
      throw new RuntimeException("Error running truck stream generator", e);
    }
  }

}

