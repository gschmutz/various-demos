package com.hortonworks.simulator.impl.domain.gps;

/**
 * Exception occuring when trying to build a path that travel back to the future
 * (i.e. when a timestamped location is back in time with regards to its ancestor in the path)
 * 
 * @author sebastienjean
 */
public class BackToTheFutureException extends Exception
{

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = 4197784044222288789L;
}
