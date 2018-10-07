package com.hortonworks.simulator.impl.domain.transport.route;//package com.hortonworks.streaming.impl.domain.transport.route;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import org.apache.log4j.Logger;
//
//import Location;
//
//public class RouteGenerator implements Route {
//
//	private static final Logger LOG = Logger.getLogger(RouteGenerator.class);
//	
//	private List<Location> locations;
//	private int locationIndex=0;
//	private Integer routeId;
//	private boolean routeEnded = false;
//
//	
//	public RouteGenerator(Location startingPoint) {
//		locations = new ArrayList<Location>();
//		locations.add(startingPoint);
//	}
//
//	public Location getStartingPoint() {
//		return locations.get(0);
//	}
//
//	public Location getNextLocation() {
//		Location location = null;
//		if(locationIndex == locations.size()) {
//			location = getNextNewLocation();
//			locations.add(location);
//		} else {
//			location = locations.get(locationIndex);
//		}
//		locationIndex++;
//		return location;
//	}
//
//	private Location getNextNewLocation() {
//		LOG.debug("Generating new Location for Route...");
//		// Get Previous location
//		Location previousLocation = locations.get(locationIndex - 1);
//		double randomLat = (Math.random() - 0.7D)/10;
//		double randomLong = (Math.random() - 0.7D) /10;
//		double randomAlt = (Math.random() - 0.7D) /10;
//		Location nextLocation = new Location(previousLocation.getLongitude() + randomLong,
//											previousLocation.getLatitude() + randomLat,
//											previousLocation.getAltitude() + randomAlt);
//		locations.add(nextLocation);
//		return nextLocation;	
//	}
//	
//	public List<Location> getLocations() {
//		return this.locations;
//	}
//
//	@Override
//	public boolean routeEnded() {
//		return routeEnded;
//	}
//
//	public int getRouteId() {
//		if(this.routeId == null) {
//			this.routeId = new Random().nextInt();
//		}
//		return this.routeId;
//	}
//
//}
