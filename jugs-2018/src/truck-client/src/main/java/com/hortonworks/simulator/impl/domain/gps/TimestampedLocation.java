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

import java.util.Calendar;

/**
 * This class represents a timestamped location, consisting in a location and a
 * date.
 * 
 * @author sebastienjean
 */
public class TimestampedLocation
{
	/**
	 * Date associated to the waypoint.
	 */
	private final Calendar date;

	/**
	 * Location associated to the waypoint.
	 */
	private final Location location;

	/**
	 * Creates a new timestamped waypoint, from given date and location.
	 *
	 * @param date
	 *            the date associated to the waypoint
	 * @param location
	 *            the location associated to the waypoint
	 */
	public TimestampedLocation(Calendar date, Location location)
	{
		super();
		this.date = date;
		this.location = location;
	}

	/**
	 * Getter for the date associated to the waypoint.
	 *
	 * @return the date associated to the waypoint
	 */
	public Calendar getDate()
	{
		return this.date;
	}

	/**
	 * Getter for the location associated to the waypoint.
	 * 
	 * @return the location associated to the waypoint
	 */
	public Location getLocation()
	{
		return this.location;
	}
	
	/**
	 * Getter for the duration between this waypoint and a destination waypoint, in milliseconds
	 * @param destination destination waypoint
	 * @return duration between this waypoint and a destination waypoint, in milliseconds
	 * @throws BackToTheFutureException  if the date of the destination waypoint is back
	 *             in time with regards to this waypoint (a null waypoint is
	 *             also considered as back in time)
	 */
	public long getDurationMillis(TimestampedLocation destination) throws BackToTheFutureException
	{
		if (destination == null) throw new BackToTheFutureException();
		
		long millis =  destination.date.getTimeInMillis() - this.date.getTimeInMillis();
		
		if (millis < 0) throw new BackToTheFutureException();
		
		return millis;
	}
	 
}
