package com.hortonworks.transport;

import java.io.Serializable;

public class TruckDriverViolationEvent implements Serializable {
	
	private static final long serialVersionUID = -4938872981439030338L;
	
	private String truckDriverEventKey;
	private int driverId;
	private int truckId;
	private long timeStamp;
	private double longitude;
	private double latitude;
	private String infractionEvent;
	private long numberOfInfractions;
	private String timeStampString;

	private String driverName;
	private int routeId;

	private String routeName;
	
	
	public TruckDriverViolationEvent(String truckDriverEventKey, int driverId, int truckId, long timeStamp, String timeStampString,
			double longitude, double latitude, String lastInfraction,
			long numberOfInfractions, String driverName, int routeId, String routeName) {
		super();
		this.truckDriverEventKey = truckDriverEventKey;
		this.driverId = driverId;
		this.truckId = truckId;
		this.timeStamp = timeStamp;
		this.timeStampString = timeStampString;
		this.longitude = longitude;
		this.latitude = latitude;
		this.infractionEvent = lastInfraction;
		this.numberOfInfractions = numberOfInfractions;
		this.driverName = driverName;
		this.routeId = routeId;
		this.routeName = routeName;
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


	public double getLongitude() {
		return longitude;
	}


	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}


	public double getLatitude() {
		return latitude;
	}


	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}


	public String getInfractionEvent() {
		return infractionEvent;
	}


	public void setInfractionEvent(String infractionEvent) {
		this.infractionEvent = infractionEvent;
	}


	public long getNumberOfInfractions() {
		return numberOfInfractions;
	}


	public void setNumberOfInfractions(long numberOfInfractions) {
		this.numberOfInfractions = numberOfInfractions;
	}


	public String getTruckDriverEventKey() {
		return truckDriverEventKey;
	}


	public void setTruckDriverEventKey(String truckDriverEventKey) {
		this.truckDriverEventKey = truckDriverEventKey;
	}


	public String getTimeStampString() {
		return timeStampString;
	}


	public void setTimeStampString(String timeStampString) {
		this.timeStampString = timeStampString;
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
