package com.hortonworks.simulator.impl.domain.gps;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a path, in other words an ordered list of timestamped
 * location.
 * 
 * @author sebastienjean
 * 
 */
public class Path {
	/**
	 * Ordered list of timestamped locations.
	 */
	private final List<TimestampedLocation> waypoints;

	/**
	 * Creates a new empty path.
	 */
	public Path() {
		this.waypoints = new LinkedList<TimestampedLocation>();
	}

	/**
	 * Getter for waypoints count.
	 * 
	 * @return waypoints count
	 */
	public int getPathWaypointsCount() {
		return this.waypoints.size();
	}

	/**
	 * Getter for path first waypoint.
	 * 
	 * @return path first waypoint (<tt>null</tt> when path is empty)
	 */
	public TimestampedLocation getStart() {
		return this.waypoints.get(0);
	}

	/**
	 * Getter for path last waypoint.
	 * 
	 * @return path last waypoint (<tt>null</tt> when path is empty)
	 */
	public TimestampedLocation getFinish() {
		return this.waypoints.get(this.waypoints.size() - 1);
	}

	/**
	 * Appends a waypoint to the path
	 * 
	 * @param waypoint
	 *            the waypoint to append
	 * @throws BackToTheFutureException
	 *             if the date of the waypoint to be added to the path is back
	 *             in time with regards to the last waypoint (a null waypoint is
	 *             also considered as back in time)
	 */
	public void addWaypoint(TimestampedLocation waypoint)
			throws BackToTheFutureException {
		if (!waypoints.isEmpty()) {
			TimestampedLocation last = this.getFinish();

			if ((waypoint == null)
					|| ((last != null) && (last.getDate().compareTo(
							waypoint.getDate()) >= 0)))
				throw new BackToTheFutureException();
		}
		this.waypoints.add(waypoint);
	}

	/**
	 * Removes the waypoint at a given offset. Does nothing if the offset is not
	 * valid (i.e. not in [0, getPathWaypointsCount[ ).
	 * 
	 * @param offset
	 *            the offset of the waypoint to remove
	 */
	public void removeWaypoint(int offset) {
		this.waypoints.remove(offset);
	}

	/**
	 * Getter for the waypoint at a given offset.
	 * 
	 * @param offset
	 *            the offset of the waypoint to retrieve
	 * @return the waypoint at offset <tt>offset</tt> in the path, <tt>null</tt>
	 *         if the offset is not valid (i.e. not in [0,
	 *         getPathWaypointsCount[ )
	 */
	public TimestampedLocation getWaypoint(int offset) {
		return this.waypoints.get(offset);
	}

	/**
	 * Getter for path duration (in milliseconds)
	 * 
	 * @return path duration (in milliseconds)
	 */
	public long getDuration() {
		return this.getFinish().getDate().getTimeInMillis()
				- this.getStart().getDate().getTimeInMillis();
	}

	/**
	 * Getter for path "true" (considering 3 dimensions) length (in meters), i.e
	 * the sum of each segment (3D) length.
	 * 
	 * @return path length (3D) in meters
	 */
	public long getTrueLength() {
		long trueLength = 0;
		for (int i = 1; i < this.waypoints.size(); i++) {
			Location start = this.waypoints.get(i - 1).getLocation();
			Location end = this.waypoints.get(i).getLocation();
			trueLength += start.get3DDistance(end);
		}
		return trueLength;
	}

	/**
	 * Getter for path "over ground" (not considering altitude) length (in
	 * meters), i.e the sum of each segment (2D) length.
	 * 
	 * @return path length (2D) in meters
	 */
	public long getOverGroundLength() {
		long overGroungLength = 0;
		for (int i = 1; i < this.waypoints.size(); i++) {
			Location start = this.waypoints.get(i - 1).getLocation();
			Location end = this.waypoints.get(i).getLocation();
			overGroungLength += start.getOverGroundDistance(end);
		}
		return overGroungLength;
	}

	/**
	 * Getter for path "true" (considering 3 dimensions) average speed (in
	 * km/h).
	 * 
	 * @return path "true" average speed in km/h
	 */
	public double getTrueAverageSpeed() {
		return this.getTrueLength() / this.getDuration();
	}

	/**
	 * Getter for path "over ground" (not considering altitude) average speed
	 * (in km/h).
	 * 
	 * @return path "over ground" average speed in km/h
	 */
	public double getOverGroundAverageSpeed() {
		return this.getOverGroundLength() / this.getDuration();
	}
}
