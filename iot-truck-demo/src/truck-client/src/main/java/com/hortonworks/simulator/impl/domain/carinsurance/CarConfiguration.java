package com.hortonworks.simulator.impl.domain.carinsurance;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CarConfiguration {
	private static int lastCarId = 10;
	private static int nextDriverId = 0;
	private static List<Driver> drivers;
	private static Logger logger = Logger.getLogger(CarConfiguration.class);

	static {
		drivers = new ArrayList<Driver>();
		Driver riskyDriver = new Driver(1, 30);
		Driver lessRiskyDriver = new Driver(2, 60);
		drivers.add(riskyDriver);
		drivers.add(lessRiskyDriver);
	}

	public synchronized static int getNextTruckId() {
		lastCarId++;
		return lastCarId;
	}

	public synchronized static Driver getNextDriver() {
		Driver nextDriver;
		try {
			nextDriver = drivers.get(nextDriverId);
		} catch (IndexOutOfBoundsException e) {
			drivers.add(new Driver(nextDriverId + 1, 100));
		}
		nextDriver = drivers.get(nextDriverId);
		logger.debug("Next Driver: " + nextDriver.toString());
		nextDriverId++;
		return nextDriver;
	}
}
