package com.hortonworks.simulator.impl.domain.carinsurance;

import akka.actor.ActorRef;
import com.hortonworks.simulator.impl.domain.AbstractEventEmitter;
import com.hortonworks.simulator.impl.messages.EmitEvent;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Car extends AbstractEventEmitter {
	private static final long serialVersionUID = 9157180698115417087L;
	private Driver driver;
	private int truckId;
	private int messageCount = 0;
	private List<DrivingEventTypeEnum> eventTypes;
	private Random rand = new Random();

	public Car() {
		driver = CarConfiguration.getNextDriver();
		truckId = CarConfiguration.getNextTruckId();
		eventTypes = Arrays.asList(DrivingEventTypeEnum.values());
	}

	public Car(Driver driver) {
		this();
		this.driver = driver;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	public DrivingEvent generateEvent() {
		messageCount++;
		if (messageCount % driver.getRiskFactor() == 0)
			return new DrivingEvent(getRandomUnsafeEvent(), this);
		else
			return new DrivingEvent(DrivingEventTypeEnum.NORMAL, this);
	}

	private DrivingEventTypeEnum getRandomUnsafeEvent() {
		return eventTypes.get(rand.nextInt(eventTypes.size() - 1));
	}

	@Override
	public String toString() {
		return new Timestamp(new Date().getTime()) + "|" + truckId + "|"
				+ driver.getDriverId() + "|";
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof EmitEvent) {
			ActorRef actor = this.context().system()
					.actorFor("akka://EventSimulator/user/eventCollector");
			Random rand = new Random();
			int sleepOffset = rand.nextInt(100);
			while (true) {
				Thread.sleep(500 + sleepOffset);
				actor.tell(generateEvent(), this.getSender());
			}
		}
	}
}
