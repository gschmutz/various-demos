package com.hortonworks.simulator.impl.domain.transport;

import com.hortonworks.simulator.datagenerator.DataGeneratorUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DriverStaticList {
	
	private static final List<Driver> DRIVER_LIST= new ArrayList<Driver>();
	private static Iterator<Driver> DRIVERS_LIST_ITERATOR = null;
	private static final int DRIVER_ID_START= 10;

	private static int lastDriverId;
	
	 static {
		int lastDriverId = DRIVER_ID_START;
		DRIVER_LIST.add(new Driver(lastDriverId++, "George Vetticaden", 30)); //risky driver
		DRIVER_LIST.add(new Driver(lastDriverId++, "Jamie Engesser", 10)); //most risky driver
		
		/* Everyone else is not risky */
		
		DRIVERS_LIST_ITERATOR = DRIVER_LIST.iterator();
		DRIVER_LIST.add(new Driver(lastDriverId++, "Paul Codding", 30));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Joe Niemiec", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Adis Cesir", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Rohit Bakshi", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Tom McCuch", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Eric Mizell", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Grant Liu", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Ajay Singh", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Chris Harris", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Jeff Markham", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Nadeem Asghar", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Adam Diaz", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Don Hilborn", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Jean-Philippe Player", 90));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Michael Aube", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Mark Lochbihler", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Olivier Renault", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Teddy Choi", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Dan Rice", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Rommel Garcia", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Ryan Templeton", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Sridhara Sabbella", 80));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Frank Romano", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Emil Siemes", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Andrew Grande", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Wes Floyd", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Scott Shaw", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "David Kaiser", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Nicolas Maillard", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Greg Phillips", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Randy Gelhausen", 100));
		DRIVER_LIST.add(new Driver(lastDriverId++, "Dave Patton", 30));
	}
	
	public synchronized static Driver next() {
		
		Driver driver = null; 
		if(DRIVERS_LIST_ITERATOR.hasNext()) {
			driver = DRIVERS_LIST_ITERATOR.next();
		} else {
			driver = createNewDriver();
		}
		return driver;
		
	}
	
	private synchronized static Driver createNewDriver() {
		String driverName = DataGeneratorUtils.getRandomString(5) + " " + DataGeneratorUtils.getRandomString(6);
		Driver driver = new Driver(lastDriverId++, driverName, 100);
		return driver;
	}

	public static void reset() {
		DRIVERS_LIST_ITERATOR = DRIVER_LIST.iterator();
		lastDriverId = DRIVER_LIST.size() + DRIVER_ID_START;
	}

	public static Driver getRiskyDriver() {
		return DRIVER_LIST.get(0);
		
	}
	
	public static Driver getMostRiskyDriver() {
		return DRIVER_LIST.get(1);
		
	}	

}

