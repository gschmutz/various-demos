package com.hortonworks.simulator.impl.domain.gps;

/**
 * This class represents a path segment, in other words a couple of timestamped
 * locations.
 * 
 * @author sebastienjean
 * 
 */
public class PathSegment
{
	/**
	 * Start timestamped location.
	 */
	private final TimestampedLocation start;

	/**
	 * Finish timestamped location.
	 */
	private final TimestampedLocation finish;
	
	/**
	 * Creates a new path segment from given timestamped locations.
	 * 
	 * @param start start timestamped location
	 * @param finish finish timestamped location
	 * @throws BackToTheFutureException  if the date of finish waypoint is back
	 *             in time with regards to the start waypoint (a null waypoint is
	 *             also considered as back in time)
	 */
	public PathSegment(TimestampedLocation start, TimestampedLocation finish) throws BackToTheFutureException
	{
		if ((start == null) || (finish == null)) throw new BackToTheFutureException(); 
		if (finish.getDate().getTimeInMillis() < start.getDate().getTimeInMillis()) throw new BackToTheFutureException();
		
		this.start = start;
		this.finish = finish;
	}

	/**
	 * Getter for start timestamped location
	 * 
	 * @return start timestamped location
	 */
	public TimestampedLocation getStart()
	{
		return this.start;
	}

	/**
	 * Getter for finish timestamped location
	 * 
	 * @return finish timestamped location
	 */
	public TimestampedLocation getFinish()
	{
		return this.finish;
	}
	
	/**
	 * Getter for path segment duration (in milliseconds)
	 * 
	 * @return path segment duration (in milliseconds)
	 */
	public long getDuration()
	{
		return this.getFinish().getDate().getTimeInMillis()
				- this.getStart().getDate().getTimeInMillis();
	}

	/**
	 * Getter for path segment "true" (considering 3 dimensions) length (in meters).
	 * 
	 * @return path segment length (3D) in meters
	 */
	public long getTrueLength()
	{
		return this.start.getLocation().get3DDistance(this.finish.getLocation());
	}

	/**
	 * Getter for path segment "over ground" (not considering altitude) length (in
	 * meters)
	 * 
	 * @return path length (2D) in meters
	 */
	public long getOverGroundLength()
	{
		return this.start.getLocation().getOverGroundDistance(this.finish.getLocation());
	}

	/**
	 * Getter for path segment "true" (considering 3 dimensions) average speed (in
	 * km/h).
	 * 
	 * @return path "true" average speed in km/h
	 */
	public double getTrueAverageSpeed()
	{
		return this.getTrueLength() / this.getDuration();
	}

	/**
	 * Getter for path segment "over ground" (not considering altitude) average speed
	 * (in km/h).
	 * 
	 * @return path "over ground" average speed in km/h
	 */
	public double getOverGroundAverageSpeed()
	{
		return this.getOverGroundLength() / this.getDuration();
	}
}
