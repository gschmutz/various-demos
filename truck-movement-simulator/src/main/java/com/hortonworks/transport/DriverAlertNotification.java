package com.hortonworks.transport;

import java.io.Serializable;

public class DriverAlertNotification implements Serializable {

	private static final long serialVersionUID = -4251077352194343747L;

	private String truckDriverEventKey;
	private int driverId;
	private int truckId;
	private long timeStamp;
	private String timeStampString;
	
	private String alertNotification;

	private String driverName;
	private int routeId;

	private String routeName;

	public DriverAlertNotification(String truckDriverEventKey, int driverId,
			int truckId, long timeStamp, String timeStampString,
			String alertMessage, String driverName, int routeId, String routeName) {
		super();
		this.truckDriverEventKey = truckDriverEventKey;
		this.driverId = driverId;
		this.truckId = truckId;
		this.timeStamp = timeStamp;
		this.timeStampString = timeStampString;
		this.alertNotification = alertMessage;
		this.driverName = driverName;
		this.routeId = routeId;
		this.routeName = routeName;
	}

	public String getTruckDriverEventKey() {
		return truckDriverEventKey;
	}

	public void setTruckDriverEventKey(String truckDriverEventKey) {
		this.truckDriverEventKey = truckDriverEventKey;
	}

	public int getDriverId() {
		return driverId;
	}

	public void setDriverId(int driverId) {
		this.driverId = driverId;
	}

	public int getTruckId() {
		return truckId;
	}

	public void setTruckId(int truckId) {
		this.truckId = truckId;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getTimeStampString() {
		return timeStampString;
	}

	public void setTimeStampString(String timeStampString) {
		this.timeStampString = timeStampString;
	}

	public String getAlertNotification() {
		return alertNotification;
	}

	public void setAlertNotification(String alertNotification) {
		this.alertNotification = alertNotification;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public int getRouteId() {
		return routeId;
	}

	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}

	public String getRouteName() {
		return routeName;
	}

	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}

	
	
}
