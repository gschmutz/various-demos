package com.hortonworks.simulator.impl.domain.transport.route;

import com.hortonworks.simulator.impl.domain.gps.Location;
import org.apache.log4j.Logger;

import java.util.List;

public class RouteProvided implements Route {

	private static final Logger LOG = Logger.getLogger(RouteProvided.class);
	
	private List<Location> locations;
	private int locationIndex=0;
	private Integer routeId;
	private boolean forward=true;
	private boolean routeEnded=false;

	private String routeName;

	
	public RouteProvided (String routeName, List<Location> locations) {
		this.locations = locations;
		this.routeName = routeName;
	}

	public Location getStartingPoint() {
		return locations.get(0);
	}

	public Location getNextLocation() {
		Location location = null;
		if(locationIndex == locations.size()) {
			//go background if if we got the end
			LOG.info("Revering Direction..");
			locationIndex--;
			forward = false;
			routeEnded = true;
		} else if(locationIndex == -1) {
			//go forward
			LOG.info("Going Original Direction...");
			locationIndex++;
			forward=true;
			routeEnded = true;
		} else
			routeEnded = false;
		location = locations.get(locationIndex);
		nextLocationIndex(); 
		return location;
	}
	
	public void nextLocationIndex() {
		if(forward) {
			locationIndex++;
		} else {
			locationIndex--;
		}
	}

	public List<Location> getLocations() {
		return this.locations;
	}

	@Override
	public boolean routeEnded() {
		return routeEnded;
	}

	public int getRouteId() {
		if(routeId == null) {
			routeId = Math.abs(routeName.hashCode());
		}
		return routeId;
	}
	
	public String getRouteName() {
		return this.routeName;
	}

}
