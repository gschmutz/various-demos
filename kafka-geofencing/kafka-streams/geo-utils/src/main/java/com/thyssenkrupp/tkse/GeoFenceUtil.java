package com.thyssenkrupp.tkse;

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;


public class GeoFenceUtil {
	private static final String OUTSIDE = "OUTSIDE";
	private static final String INSIDE = "INSIDE";
	private static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	private static WKTReader wktReader = new WKTReader(geometryFactory);

	private static boolean geofenceImpl(final double latitude, final double longitude, String geometryWKT) {
		boolean status = false;
		
		Polygon polygon = null;
		try {
			polygon = (Polygon) wktReader.read(geometryWKT);
	
			// However, an important point to note is that the longitude is the X value 
			// and the latitude the Y value. So we say "lat/long", 
			// but JTS will expect it in the order "long/lat". 
			Coordinate coord = new Coordinate(longitude, latitude);
			Point point = geometryFactory.createPoint(coord);
			
			status = point.within(polygon);
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage());
		}
		return status;
	}
	
	private static String geofenceStringImpl(final double latitude, final double longitude, String geometryWKT) {
		boolean within = geofenceImpl(latitude, longitude, geometryWKT);
		String status = null;
		
		if (within) {
			status = INSIDE;
		} else {
			status = OUTSIDE;
		}
		
		return status;
	}
	
	
	/** 
	 * Determines if a lat/long is inside or outside the geometry passed as the third parameter as a WKT coded string. 
	 * Returns either INSIDE or OUSIDE.
	 * @param latitude
	 * @param longitude
	 * @param geometryWKT
	 * @return
	 */
	public static boolean geofence(final double latitude, final double longitude, String geometryWKT) {
		return geofenceImpl(latitude, longitude, geometryWKT);
	}
	
	
	/** 
	 * Determines if a lat/long is inside or outside the geometry passed as the third parameter as a WKT coded string. 
	 * Returns either INSIDE or OUSIDE.
	 * @param latitude
	 * @param longitude
	 * @param geometryWKT
	 * @return
	 */
	public static String geofenceString(final double latitude, final double longitude, String geometryWKT) {
		return geofenceStringImpl(latitude, longitude, geometryWKT);
	}



		
	/** 
	 * Encode lat/long to geohash of specified length.
	 * @param latitude
	 * @param longitude
	 * @param statusBefore
	 * @param geometryWKT
	 * @return
	 */
	public static String geofence(final double latitude, final double longitude, final String statusBefore, String geometryWKT) {

		String status = geofenceStringImpl(latitude, longitude, geometryWKT);
		if (statusBefore.equals("INSIDE") && status.equals("OUTSIDE")) {
			status = "LEAVING";
		} else if (statusBefore.equals("OUTSIDE") && status.equals("INSIDE")) {
			status = "ENTERING";
		}
		
		return status;
	}
	

	/** 
	 * Encode lat/long to geohash of specified length.
	 * @param latitude
	 * @param longitude
	 * @param idGeometryListWKT
	 * @return
	 */
	public static List<String> geofence(final double latitude, final double longitude, List<String> idGeometryListWKT) {
		List<String> list = new ArrayList<>();

		for (String idGeometryWKT : idGeometryListWKT) {
			String id = idGeometryWKT.split(":")[0];
			String geometryWKT = idGeometryWKT.split(":")[1];
			list.add(id + ":" + geofenceImpl(latitude, longitude, geometryWKT));
		}
		
		return list;
	}

	/**
	 * Encode lat/long to geohash of specified length.
	 * @param latitude
	 * @param longitude
	 * @param geometryListId
	 * @param geometryListWKT
	 * @return
	 */
	public static List<String> geofence(final double latitude, final double longitude, List<String> geometryListId, List<String> geometryListWKT) {
		List<String> list = new ArrayList<>();

		int i = 0;
		for (String geometryWKT : geometryListWKT) {
			list.add(geometryListId.get(i) + ":" + geofenceImpl(latitude, longitude, geometryWKT));
			i++;
		}
		
		return list;
	}


}
