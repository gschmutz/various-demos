package com.trivadis;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.Edge;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.Vertex;
import com.trivadis.domain.Tweet;

public class GraphRepositorySingle implements Serializable, GraphRepository {

	private static final long serialVersionUID = 4725813855174838651L;

	private transient DseSession session = null;
	
	private String cassandraHost;
	private String cassandraPort;
	private String graphName;
	
	public GraphRepositorySingle(String cassandraHost, String cassandraPort, String graphName) {
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

		Vertex userVertex = VertexHelper.createOrUpdateVertex(session,
				false,
				SocialGraphConstants.TWITTER_USER_VERTEX_LABEL, 
				SocialGraphConstants.ID_PROPERTY_KEY, tweetDO.getUser().getId(), 
				SocialGraphConstants.NAME_PROPERTY_KEY, tweetDO.getUser().getScreenName() != null ? tweetDO.getUser().getScreenName().toLowerCase() : null,
				SocialGraphConstants.LANGUAGE_PROPERTY_KEY, tweetDO.getUser().getLanguage() != null ? tweetDO.getUser().getLanguage().toLowerCase() : null,
				SocialGraphConstants.VERIFIED_PROPERTY_KEY, tweetDO.getUser().getVerified() != null ? tweetDO.getUser().getVerified() : false);
		
		Vertex tweetVertex = VertexHelper.createOrUpdateVertex(session,
				false,
				SocialGraphConstants.TWEET_VERTEX_LABEL, 
				SocialGraphConstants.ID_PROPERTY_KEY, tweetDO.getId(), 
				SocialGraphConstants.TIME_PROPERTY_KEY, tweetDO.getCreatedAt().toDate().getTime(), 
				SocialGraphConstants.LANGUAGE_PROPERTY_KEY, tweetDO.getLanguage() != null ? tweetDO.getLanguage().toLowerCase() : null);
		
		Edge publishedBy = VertexHelper.createOrUpdateEdge(session, true, SocialGraphConstants.PUBLISHES_EDGE_LABEL,
				userVertex, tweetVertex);
		
		for (String term : tweetDO.getHashtags()) {
			Vertex termVertex = VertexHelper.createOrUpdateVertex(session,
					false,
					SocialGraphConstants.TERM_VERTEX_LABEL,
					SocialGraphConstants.NAME_PROPERTY_KEY, term.toLowerCase(),
					SocialGraphConstants.TYPE_PROPERTY_KEY, "hashtag");
		
			Edge replyToEdge = VertexHelper.createOrUpdateEdge(session, true, SocialGraphConstants.USES_EDGE_LABEL,
					tweetVertex, termVertex);
		}
		
		return null;
	}


}
