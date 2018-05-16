package com.hortonworks.simulator.impl.domain.transport;

import java.sql.Timestamp;
import java.util.Date;

import com.hortonworks.simulator.impl.domain.Event;
import com.hortonworks.simulator.impl.domain.gps.Location;

public class MobileEyeEvent extends Event {
	private MobileEyeEventTypeEnum eventType;
	private Truck truck;
	private Location location;
	private long correlationId;

	public MobileEyeEvent(long correlationId, Location location, MobileEyeEventTypeEnum eventType,
			Truck truck) {
		this.location = location;
		this.eventType = eventType;
		this.truck = truck;
		this.correlationId = correlationId;
	}

	public MobileEyeEventTypeEnum getEventType() {
		return eventType;
	}

	public void setEventType(MobileEyeEventTypeEnum eventType) {
		this.eventType = eventType;
	}

	public Location getLocation() {
		return location;
	}
	
	public Truck getTruck() {
		return this.truck;
	}

	@Override
	public String toString() {
		return truck.toString() + eventType.toString() + ","
				+ location.getLatitude() + "," + location.getLongitude() + "," + correlationId;
	}
	
	public String toCSV() {
		return truck.toCSV() + eventType.toCSV() + ","
				+ location.getLatitude() + "," + location.getLongitude() + "," + correlationId;
	}

	public String toJSON() { 
		StringBuffer msg = new StringBuffer();
		msg.append("{");
		msg.append("\"timestamp\":" + new Date().getTime());
		msg.append(",");
		msg.append("\"truckId\":" + truck.getTruckId());
		msg.append(",");
		msg.append("\"driverId\":" + truck.getDriver().getDriverId());
		msg.append(",");
		msg.append("\"routeId\":" + truck.getDriver().getRoute().getRouteId());
		msg.append(",");
		msg.append("\"eventType\":\"" + eventType + "\"");
		msg.append(",");
		msg.append("\"latitude\":" + location.getLatitude());
		msg.append(",");
		msg.append("\"longitude\":" + location.getLongitude());
		msg.append(",");
		msg.append("\"correlationId\":\"" + correlationId + "\"");
		msg.append("}");
		return msg.toString();
	}
	
}
