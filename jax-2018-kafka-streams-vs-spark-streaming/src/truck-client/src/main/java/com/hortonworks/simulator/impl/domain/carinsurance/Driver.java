package com.hortonworks.simulator.impl.domain.carinsurance;

import com.hortonworks.simulator.interfaces.DomainObject;

public class Driver implements DomainObject {
	private int driverId;
	private int riskFactor;

	public Driver() {
	}

	public Driver(int driverId, int riskFactor) {
		this.driverId = driverId;
		this.riskFactor = riskFactor;
	}

	public int getDriverId() {
		return driverId;
	}

	public void setDriverId(int driverId) {
		this.driverId = driverId;
	}

	public int getRiskFactor() {
		return riskFactor;
	}

	public void setRiskFactor(int riskFactor) {
		this.riskFactor = riskFactor;
	}

	@Override
	public String toString() {
		return this.driverId + "|" + this.riskFactor;
	}
}
