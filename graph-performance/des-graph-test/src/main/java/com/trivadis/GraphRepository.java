package com.trivadis;

import com.trivadis.domain.Tweet;

public interface GraphRepository {

	/**
	 * Creates the graph representation of a Tweet and the User being the author of the tweet 
	 * @param tweetDO
	 */
	GraphMetrics createTweetAndUsers(Tweet tweetDO);

}