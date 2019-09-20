package com.thyssenkrupp.tkse;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

public class DistanceUtil {

	private static CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

	public static Coordinate findClosest(Point spot, Geometry geometryToFindClosest) {
		Coordinate closest = DistanceOp.nearestPoints(geometryToFindClosest, spot)[0];
		return closest;
	}
	
	public static double calculateDistanceInMeters(Coordinate start, Coordinate end) throws TransformException {
		GeodeticCalculator gc = new GeodeticCalculator(crs);
		gc.setStartingPosition(JTS.toDirectPosition(start, crs));
		gc.setDestinationPosition(JTS.toDirectPosition(end, crs));
		double distanceInMeters = gc.getOrthodromicDistance();
		return distanceInMeters;
	}
	
	public static double calculateDistanceToPolygon(Point spot, Polygon polygon) throws TransformException {

		Coordinate closestInBox = DistanceOp.nearestPoints(polygon, spot)[0];
		double distance = calculateDistanceInMeters(spot.getCoordinate(), closestInBox);
		
		if (distance == 0) {
			// If distance is null, the point is located inside the polygon.
			// We then calculate the polygon's boundary (i.e. only the outer line),...
			Geometry polygonBoundary = polygon.getBoundary();
			// ...calculate the distance to the corresponding closest points
			Coordinate closestOnBorder = DistanceOp.nearestPoints(polygonBoundary, spot)[0];
			distance = calculateDistanceInMeters(spot.getCoordinate(), closestOnBorder);
			distance = distance * -1;
		}
		return distance;
	}
}
