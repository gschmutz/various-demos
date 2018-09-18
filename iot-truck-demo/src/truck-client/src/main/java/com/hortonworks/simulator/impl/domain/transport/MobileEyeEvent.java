package com.hortonworks.simulator.impl.domain.transport;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hortonworks.simulator.impl.domain.Event;
import com.hortonworks.simulator.impl.domain.gps.Location;
import com.hortonworks.solution.Lab;

public class MobileEyeEvent extends Event {
	public final static Integer EVENT_KIND_BEHAVIOUR_AND_POSITION=1;
	public final static Integer EVENT_KIND_POSITION=2;
	public final static Integer EVENT_KIND_BEHAVIOUR=3;
	
	
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
	
	public String toCSV(Integer eventKind) {
		String result = null;

		if (eventKind.equals(EVENT_KIND_BEHAVIOUR_AND_POSITION)) {
			result = truck.toCSV() + eventType.toCSV() + ","
				+ location.getLatitude() + "," + location.getLongitude() + "," + correlationId;
		} else if (eventKind.equals(EVENT_KIND_POSITION)){
			result = new Date().getTime() + "," + truck.getTruckId() + "," + location.getLatitude() + "," + location.getLongitude();
		} else if (eventKind.equals(EVENT_KIND_BEHAVIOUR)){
			result = new Date().getTime() + "," + truck.getTruckId() + eventType.toCSV() + "," + correlationId;
		}
		return result;
	}

	public String toJSON(Integer eventKind) { 
		StringBuffer msg = new StringBuffer();
		msg.append("{");
		msg.append("\"timestamp\":" + new Date().getTime());
		msg.append(",");
		msg.append("\"truckId\":" + truck.getTruckId());
		msg.append(",");
		if (eventKind.equals(EVENT_KIND_BEHAVIOUR_AND_POSITION) || eventKind.equals(EVENT_KIND_BEHAVIOUR)) {
			msg.append("\"driverId\":" + truck.getDriver().getDriverId());
			msg.append(",");
			msg.append("\"routeId\":" + truck.getDriver().getRoute().getRouteId());
			msg.append(",");
			msg.append("\"eventType\":\"" + eventType + "\"");
			msg.append(",");
		}
		if (eventKind.equals(EVENT_KIND_BEHAVIOUR_AND_POSITION) || eventKind.equals(EVENT_KIND_POSITION)) {
			msg.append("\"latitude\":" + location.getLatitude());
			msg.append(",");
			msg.append("\"longitude\":" + location.getLongitude());
		}
		if (eventKind.equals(EVENT_KIND_BEHAVIOUR_AND_POSITION) || eventKind.equals(EVENT_KIND_BEHAVIOUR)) {
			msg.append("\"correlationId\":\"" + correlationId + "\"");
			msg.append("}");
		}
		return msg.toString();
	}
	
}
