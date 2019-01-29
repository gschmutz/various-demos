package com.trivadis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphOptions;
import com.trivadis.domain.Tweet;

public class GraphRepositoryGroovyScript implements Serializable, GraphRepository {

	private static final long serialVersionUID = 4725813855174838651L;

	//private transient GraphRepositoryUtilDseGraph util = new GraphRepositoryUtilDseGraph();
	
	private transient DseSession session = null;
	
	private String cassandraHost;
	private String cassandraPort;
	private String graphName;

	public GraphRepositoryGroovyScript(String cassandraHost, String cassandraPort, String graphName) {
		this.cassandraHost = cassandraHost;			
		 DseCluster dseCluster = DseCluster.builder()
		        .addContactPoints(StringUtils.split(cassandraHost,","))
		        .withGraphOptions(new GraphOptions().setGraphName(graphName)
						//.setReadTimeoutMillis(readTimeoutMillis)
						.setGraphReadConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
						.setGraphWriteConsistencyLevel(ConsistencyLevel.ONE))
		        .build();
		session = dseCluster.connect();

		this.cassandraPort = cassandraPort;
		this.graphName = graphName;

	}

	public DseSession getDseSession() {
		return session;
	}

	/* ======================= implementation =================================*/

	/* (non-Javadoc)
	 * @see com.trivadis.GraphRepository#createTweetAndUsers(com.trivadis.domain.Tweet, boolean, boolean)
	 */
	@Override
	public GraphMetrics createTweetAndUsers(Tweet tweetDO) {
		DseGraphDynGremlinHelper dyn = new DseGraphDynGremlinHelper(session);
		List<Map<String,Object>> paramsList = null;
		
		// ============================= User =========================================
		
		dyn.addCreateVertex("user", SocialGraphConstants.TWITTER_USER_VERTEX_LABEL
															, SocialGraphConstants.ID_PROPERTY_KEY, tweetDO.getUser().getIdAsString()
															, SocialGraphConstants.NAME_PROPERTY_KEY, tweetDO.getUser().getScreenName() != null ? tweetDO.getUser().getScreenName().toLowerCase() : null
															, SocialGraphConstants.LANGUAGE_PROPERTY_KEY, tweetDO.getUser().getLanguage() != null ? tweetDO.getUser().getLanguage().toLowerCase() : null
															, SocialGraphConstants.VERIFIED_PROPERTY_KEY, tweetDO.getUser().getVerified() != null ? tweetDO.getUser().getVerified() : false
															);
	
		// ============================= Tweet =========================================

		dyn.addCreateVertex("tweet", SocialGraphConstants.TWEET_VERTEX_LABEL
				, SocialGraphConstants.ID_PROPERTY_KEY, tweetDO.getId().toString() 
				, SocialGraphConstants.TIME_PROPERTY_KEY, tweetDO.getCreatedAt().toDate().getTime() 
				, SocialGraphConstants.LANGUAGE_PROPERTY_KEY, tweetDO.getLanguage() != null ? tweetDO.getLanguage().toLowerCase() : null);
		dyn.addCreateEdge("publishes", "user", "tweet", SocialGraphConstants.PUBLISHES_EDGE_LABEL);
							//		, SocialGraphConstants.TIME_PROPERTY_KEY, new DateTime().toDate().getTime());

		// ============================= Hashtags ======================================
		
		dyn.addCreateVertices("term", 
									SocialGraphConstants.TERM_VERTEX_LABEL, 
									SocialGraphConstants.NAME_PROPERTY_KEY, 
									new ArrayList<Object>(tweetDO.getHashtags().stream().map(String::toLowerCase).collect(Collectors.toList())), 
									SocialGraphConstants.TYPE_PROPERTY_KEY, "hashtag");
			
		dyn.addCreateEdges("usesTerm", 
								"tweet", 1, 
								"term", tweetDO.getHashtags().size(),
								SocialGraphConstants.USES_EDGE_LABEL);

		dyn.execute("createTweetAndUsersImpl");
		
		return dyn.getGraphMetrics();
	}


}
