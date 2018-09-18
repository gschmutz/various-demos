package com.hortonworks.simulator.impl.domain.transport.route;

import com.hortonworks.simulator.impl.domain.gps.Location;

import java.util.List;

public interface Route {
	List<Location> getLocations();
	Location getNextLocation();
	Location getStartingPoint();
	boolean routeEnded();
	int getRouteId();
	String getRouteName();
}