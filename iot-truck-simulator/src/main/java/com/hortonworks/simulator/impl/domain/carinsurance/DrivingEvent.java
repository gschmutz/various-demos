package com.hortonworks.simulator.impl.domain.carinsurance;

import com.hortonworks.simulator.impl.domain.Event;

public class DrivingEvent extends Event {
	private DrivingEventTypeEnum eventType;
	private Car truck;

	public DrivingEvent() {
	}

	public DrivingEvent(DrivingEventTypeEnum eventType, Car truck) {
		this.eventType = eventType;
		this.truck = truck;
	}

	public DrivingEventTypeEnum getEventType() {
		return eventType;
	}

	public void setEventType(DrivingEventTypeEnum eventType) {
		this.eventType = eventType;
	}
	
	@Override
	public String toString() {
		return truck.toString() + eventType.toString();
	}
}
