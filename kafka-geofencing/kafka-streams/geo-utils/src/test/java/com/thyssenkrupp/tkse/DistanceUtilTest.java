package com.thyssenkrupp.tkse;

import java.util.Locale;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class DistanceUtilTest {
	
	private static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	
	@Test
	public void near() throws Exception {
		this.calculateDistanceToRhine(6.688356399536133, 51.5295183372046);
	}
	
	@Test
	public void far() throws Exception {
		this.calculateDistanceToRhine(6.894950866699219, 51.58282994156007);
	}
	
	public double calculateDistanceToRhine(double longitude, double latitude) throws Exception {
		Coordinate spotCoordinate = new Coordinate(longitude, latitude);
		Point spotPoint = geometryFactory.createPoint(spotCoordinate);

		Coordinate closestOnRhine = RhineUtil.calculateClosestPointOnRhine(spotPoint);
		double distanceInMeters = DistanceUtil.calculateDistanceInMeters(spotCoordinate, closestOnRhine);
		
		System.out.printf(Locale.US,
				"Distance from POINT(%f %f) to POINT(%f %f) [LINESTRING(%f %f, %f %f)] is: %f.%n", 
				spotCoordinate.getX(), spotCoordinate.getY(), 
				closestOnRhine.getX(), closestOnRhine.getY(), 
				spotCoordinate.getX(), spotCoordinate.getY(), 
				closestOnRhine.getX(), closestOnRhine.getY(), 
				distanceInMeters);

		return distanceInMeters;
	}
}
