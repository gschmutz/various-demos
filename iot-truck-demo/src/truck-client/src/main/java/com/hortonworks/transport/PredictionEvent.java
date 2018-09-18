package com.hortonworks.transport;

import java.io.Serializable;

public class PredictionEvent implements Serializable {
	
	private static final long serialVersionUID = -4938872981439030339L;
	
	public PredictionEvent(String prediction, String driverName,
			String routeName, int driverId, int truckId, String timeStamp,
			double longitude, double latitude, String certified,
			String wagePlan, int hours_logged, int miles_logged,
			String isFoggy, String isRainy, String isWindy) {
		super();
		this.prediction = prediction;
		this.driverName = driverName;
		this.routeName = routeName;
		this.driverId = driverId;
		this.truckId = truckId;
		this.timeStamp = timeStamp;
		this.longitude = longitude;
		this.latitude = latitude;
		this.certified = certified;
		this.wagePlan = wagePlan;
		this.hours_logged = hours_logged;
		this.miles_logged = miles_logged;
		this.isFoggy = isFoggy;
		this.isRainy = isRainy;
		this.isWindy = isWindy;
	}
	
	public String getPrediction() {
		return prediction;
	}
	public void setPrediction(String prediction) {
		this.prediction = prediction;
	}
	public String getDriverName() {
		return driverName;
	}
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	public String getRouteName() {
		return routeName;
	}
	public void setRouteName(String routeName) {
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
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
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
	public String getCertified() {
		return certified;
	}
	public void setCertified(String certified) {
		this.certified = certified;
	}
	public String getWagePlan() {
		return wagePlan;
	}
	public void setWagePlan(String wagePlan) {
		this.wagePlan = wagePlan;
	}
	public int getHours_logged() {
		return hours_logged;
	}
	public void setHours_logged(int hours_logged) {
		this.hours_logged = hours_logged;
	}
	public int getMiles_logged() {
		return miles_logged;
	}
	public void setMiles_logged(int miles_logged) {
		this.miles_logged = miles_logged;
	}
	public String getIsFoggy() {
		return isFoggy;
	}
	public void setIsFoggy(String isFoggy) {
		this.isFoggy = isFoggy;
	}
	public String getIsRainy() {
		return isRainy;
	}
	public void setIsRainy(String isRainy) {
		this.isRainy = isRainy;
	}
	public String getIsWindy() {
		return isWindy;
	}
	public void setIsWindy(String isWindy) {
		this.isWindy = isWindy;
	}
	private String prediction;
	private String driverName;
	private String routeName;
	private int driverId;
	private int truckId;
	private String timeStamp;
	private double longitude;
	private double latitude;
	private String certified;
	private String wagePlan;
	private int hours_logged;
	private int miles_logged;
	private String isFoggy;
	private String isRainy;
	private String isWindy;

}

