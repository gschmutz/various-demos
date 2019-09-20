package com.thyssenkrupp.tkse;

import java.util.HashMap;
import java.util.Map;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

public class GeoUtil {
	private static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	private static WKTReader reader = new WKTReader(geometryFactory);
	private static WKTWriter writer = new WKTWriter();
	
	/** 
	 * Gets the Centroid of a given Geometry provided in WKT format
	 * @param geometryWKT	a given geometry in WKT format
	 * @return	returns the centroid as a position defined in WKT format
	 */
	public static String centroidWKT(String geometryWKT) {
		String pointWKT = "";
		
		try {
			Geometry geometry = reader.read(geometryWKT);
			Point point = geometry.getCentroid();
			pointWKT = writer.write(point);
			
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage());
		}
		return pointWKT;
	}
	

}
