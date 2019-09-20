package com.thyssenkrupp.tkse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.measure.Longitude;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import com.github.davidmoten.geo.Coverage;
import com.github.davidmoten.geo.Direction;
import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;



public class GeoHashUtil {
	private static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	private static WKTReader reader = new WKTReader(geometryFactory);
	
	/** 
	 * Returns the adjacent hash in given Direction. Direction should be one of TOP, BOTTOM, LEFT, RIGHT"
	 * @param geohash
	 * @param directionString
	 * @return
	 */
	public static String adjacentHash(String geohash, String directionString) {
		Direction direction = Direction.valueOf(directionString.toUpperCase());
		if (direction == null) {
			throw new IllegalArgumentException("Invalid direction parameter, needs to be either TOP, BOTTON, RIGHT or LEFT");
		}
		return GeoHash.adjacentHash(geohash, direction);
	}
	
	/**
	 * Returns a list of the 8 surrounding hashes for a given hash in order left,right,top,bottom,left-top,left-bottom,right-top,right-bottom
	 */ 
	public static List<String> neighbours(String geohash) {
		return GeoHash.neighbours(geohash);
	}

	/** encode lat/long to geohash of specified length
	 * 
	 * @param latitude
	 * @param longitude
	 * @param length
	 * @return
	 */
	public static String geohash(final double latitude, final double longitude, int length) {
		return GeoHash.encodeHash(latitude, longitude, length);
	}

	/** 
	 * encode lat/long to geohash.")
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public static String geohash(final double latitude, final double longitude) {
		return GeoHash.encodeHash(latitude, longitude);
	}

	/**
	 * 
	 * @param geohash
	 * @return
	 */
	public static Map<String, Double> decodeHash(String geohash){
		Map<String, Double> latLongMap = new HashMap<>();
		
		LatLong latLong = GeoHash.decodeHash(geohash);
		latLongMap.put("longitude", latLong.getLat());
		latLongMap.put("latitude", latLong.getLon());
		
		return latLongMap;
	}
	
	/**
	 * Returns the maximum length of hash that covers the bounding box. If no hash can enclose the bounding box then 0 is returned
	 */
	public static int hashLengthToCoverBoundingBox(double topLeftLatitude, double topLeftLongitude, 
									double bottomRightLatitude, double bottomRightLongitude) {
		return GeoHash.hashLengthToCoverBoundingBox(topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude);
	}
	
	
	/**
	 * Returns true if and only if the bounding box corresponding to the hash contains the given latitude and longitude.
	 * @param geoHash
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public static boolean hashContains (String geoHash, double latitude, double longitude) {
		return GeoHash.hashContains(geoHash, latitude, longitude);
	}
	
	/** 
	 * Encode geohases which cover bounding box around geometries
	 * @param geometryWKT
	 * @param length
	 * @return
	 */
	public static List<String> coverBoundingBox(String geometryWKT, int length) {
		List<String> list = new ArrayList<>();
		Geometry geometry;
		try {
			geometry = reader.read(geometryWKT);

			// returns the bounding box
			Envelope e = geometry.getEnvelopeInternal();
			Coverage c = GeoHash.coverBoundingBox(e.getMaxY(), e.getMinX(), e.getMinY(), e.getMaxX(), length);
			list = new ArrayList<String>(c.getHashes());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	/** 
	 * Returns a list of geohashes the rectangle specified by the topLeftCorner and bottonRightCorner overlaps.
	 * @param topLeftLatitude		Latitude of the upper left corner
	 * @param topLeftLongitude  	Longitude of upper left corner
	 * @param bottomRightLatitude	Latitude of the lower right corner
	 * @param bottomRightLongitude	Longitude of lower right corner
	 * @param length
	 * @return
	 */
	public static List<String> coverBoundingBox(Double topLeftLatitude, Double topLeftLongitude,  
								Double bottomRightLatitude, Double bottomRightLongitude, int length) {
		List<String> list = new ArrayList<>();
		Coverage c = GeoHash.coverBoundingBox(topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude, length);
		list = new ArrayList<String>(c.getHashes());
		
		return list;
	}
}
