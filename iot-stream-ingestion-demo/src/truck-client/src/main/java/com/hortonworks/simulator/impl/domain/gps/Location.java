/*
 Copyright (C) 2013 Sebastien Jean <baz dot jean at gmail dot com>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the version 3 GNU General Public License as
 published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hortonworks.simulator.impl.domain.gps;

/**
 * This class represents a location, consisting in longitude/latitude/altitude
 * elements.
 * 
 * @author sebastienjean
 */
public class Location
{
	/**
	 * Earth radius in km.
	 */
	private final static double EARTH_RADIUS_METERS = 6371000.0;

	// Spherical coordinates
	/**
	 * Location longitude (in +/-dd.d).
	 */
	private final double longitude;

	/**
	 * Location latitude (in +/-dd.d).
	 */
	private final double latitude;

	/**
	 * Location latitude (in meters).
	 */
	private final double altitude;

	// Cartesian coordinates

	// x = r * cos(latitude) * cos(longitude);
	// y = r * cos(latitude) * sin(longitude);
	// z = r * sin(latitude);

	/**
	 * X-axis coordinate
	 */
	private final double x;

	/**
	 * Y-axis coordinate
	 */
	private final double y;

	/**
	 * Z-axis coordinate
	 */
	private final double z;

	/**
	 * Creates a new location, from given longitude/latitude/altitude.
	 * 
	 * @param longitude
	 *            location longitude (in +/-dd.d)
	 * @param latitude
	 *            location latitude (in +/-dd.d)
	 * @param altitude
	 *            location altitude (in meters)
	 */
	public Location(double longitude, double latitude, double altitude)
	{
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;

		double longitudeRadians = Math.toRadians(this.longitude);
		double latitudeRadians = Math.toRadians(this.latitude);
		double h = (EARTH_RADIUS_METERS + altitude);

		this.x = h * Math.cos(latitudeRadians) * Math.cos(longitudeRadians);
		this.y = h * Math.cos(latitudeRadians) * Math.sin(longitudeRadians);
		this.z = h * Math.sin(latitudeRadians);
	}

	/**
	 * Getter for location longitude (in +/-dd.d).
	 * 
	 * @return location longitude
	 */
	public double getLongitude()
	{
		return this.longitude;
	}

	/**
	 * Getter for location latitude (in +/-dd.d).
	 * 
	 * @return location latitude
	 */
	public double getLatitude()
	{
		return this.latitude;
	}

	/**
	 * Getter for location altitude (in meters).
	 * 
	 * @return location altitude
	 */
	public double getAltitude()
	{
		return this.altitude;
	}

	/**
	 * Getter for "true" distance (3D) to another point (in meters).
	 * 
	 * @param destination
	 *            destination location
	 * @return distance (3D), in meters.
	 */
	public long get3DDistance(Location destination)
	{
		// Approximated considering that the curve between location and
		// destination is a line
		double altitudeDelta = destination.altitude - this.altitude;
		double overGroundDistance = this.getOverGroundDistance(destination);

		return (long) (Math.sqrt(Math.pow(altitudeDelta, 2)
				+ Math.pow(overGroundDistance, 2)));
	}

	/**
	 * Getter for "true" distance (3D) to another point (in meters), using
	 * cartesian coordinates method
	 * 
	 * @param destination
	 *            destination location
	 * @return distance (3D), in meters.
	 */
	public long get3DDistanceFromCartesianCoordinates(Location destination)
	{
		return (long) (Math.sqrt(Math.pow(destination.x - this.x, 2)
				+ Math.pow(destination.y - this.y, 2)
				+ Math.pow(destination.z - this.z, 2)));
	}

	/**
	 * Getter for "over ground" distance (2D) to another point (in meters).
	 * 
	 * @param destination
	 *            destination location
	 * @return distance (2D), in meters.
	 */
	public long getOverGroundDistance(Location destination)
	{
		double longitudeDeltaRadians = Math.toRadians(destination.longitude
				- this.longitude);

		double latitudeDeltaRadians = Math.toRadians(destination.latitude
				- this.latitude);

		double sineSquareHalfLongitudeDelta = Math.pow(
				Math.sin(longitudeDeltaRadians / 2), 2);

		double sineSquareHalfLatitudeDelta = Math.pow(
				Math.sin(latitudeDeltaRadians / 2), 2);

		double sum = sineSquareHalfLatitudeDelta
				+ (sineSquareHalfLongitudeDelta * (Math.cos(Math
						.toRadians(this.latitude)) * Math.cos(Math
						.toRadians(destination.latitude))));

		return (long) (2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(sum)));
	}

	/**
	 * Getter for azimuth to destination.
	 * 
	 * @param destination
	 *            destination location
	 * @return azimuth angle from North (in degrees) to destination location
	 */
	public double getAzimuth(Location destination)
	{
		Location destinationWithThisLatitude = new Location(
				destination.longitude, this.latitude, destination.altitude);

		long dLongitude = this
				.getOverGroundDistance(destinationWithThisLatitude);
		long dLatitude = destinationWithThisLatitude
				.getOverGroundDistance(destination);

		double angle = 90.0 - Math.toDegrees(Math
				.atan(((double) dLatitude / (double) dLongitude)));

		if (destination.longitude > this.longitude)
		{
			if (destination.latitude < this.latitude)
				angle = 180 - angle;
		}
		else
		{
			if (destination.latitude > this.latitude)
				angle = 360.0 - angle;
			else
				angle = 180.0 + angle;
		}

		return angle;
	}

	/**
	 * Getter for elevation to destination.
	 * 
	 * @param destination
	 *            destination location
	 * @return elevation angle (in degrees) to destination location
	 */
	public double getElevation(Location destination)
	{
		long distance3D = this
				.get3DDistanceFromCartesianCoordinates(destination);

		double elevation = Math.toDegrees(Math.asin(Math
				.abs(destination.altitude - this.altitude) / distance3D));

		if (this.altitude > destination.altitude)
			elevation = -elevation;

		return elevation;
	}
}
