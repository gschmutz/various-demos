package com.trivadis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.Edge;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.Vertex;
import com.trivadis.domain.Tweet;

public class GraphIngestStrategies implements Serializable {

	private static final long serialVersionUID = 4725813855174838651L;

	private transient DseSession session = null;
	
	private String cassandraHost;
	private String cassandraPort;
	private String graphName;
	
	private Map<Object,Vertex> cache;
	
	public int cacheHit = 0;
	public int cacheMiss = 0;
	
	private Object fmtKey(String label, String id) {
		return label + ":" + id;
	}
	
	private Vertex getFromCache(String label, String id) {
		Vertex v = cache.get(fmtKey(label, id));
		if (v != null) { cacheHit++; } else { cacheMiss++; }
		return v;
	}
	private void addToCache(String label, String id, Vertex v) {
		cache.put(fmtKey(label,id), v);
	}

	private Vertex getFromCache(String label, Long id) {
		return getFromCache(label, id.toString());
	}
	private void addToCache(String label, Long id, Vertex v) {
		addToCache(label, id.toString(), v);
	}
	
	public void clearCache() {
		cache.clear();
		cacheHit = 0;
		cacheMiss = 0;
	}
	
	public GraphIngestStrategies(String cassandraHost, String cassandraPort, String graphName) {
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
		// init cache
		cache = new HashMap<Object,Vertex>();
	}

	public DseSession getDseSession() {
		return session;
	}

	/* ======================= implementation =================================*/

	/* (non-Javadoc)
	 * @see com.trivadis.GraphRepository#createTweetAndUsers(com.trivadis.domain.Tweet, boolean, boolean)
	 */
	public void createSingle(Tweet tweetDO, boolean useCustomVertexId) {
		String suffix = (useCustomVertexId ? "CV" : "");

		Vertex userVertex = VertexHelper.getVertexByLabelAndPropertyKey(session,
				SocialGraphConstants.TWITTER_USER_VERTEX_LABEL + suffix, 
				SocialGraphConstants.ID_PROPERTY_KEY, tweetDO.getUser().getId());
		if (userVertex == null) {
			userVertex = VertexHelper.createVertex(session,
				SocialGraphConstants.TWITTER_USER_VERTEX_LABEL + suffix, 
				SocialGraphConstants.ID_PROPERTY_KEY, tweetDO.getUser().getId(), 
				SocialGraphConstants.NAME_PROPERTY_KEY, tweetDO.getUser().getScreenName() != null ? tweetDO.getUser().getScreenName().toLowerCase() : null,
				SocialGraphConstants.LANGUAGE_PROPERTY_KEY, tweetDO.getUser().getLanguage() != null ? tweetDO.getUser().getLanguage().toLowerCase() : null,
				SocialGraphConstants.VERIFIED_PROPERTY_KEY, tweetDO.getUser().getVerified() != null ? tweetDO.getUser().getVerified() : false);
		}
		Vertex tweetVertex = VertexHelper.createVertex(session,
				SocialGraphConstants.TWEET_VERTEX_LABEL + suffix, 
				SocialGraphConstants.ID_PROPERTY_KEY, tweetDO.getId(), 
				SocialGraphConstants.TIME_PROPERTY_KEY, tweetDO.getCreatedAt().toDate().getTime(), 
				SocialGraphConstants.LANGUAGE_PROPERTY_KEY, tweetDO.getLanguage() != null ? tweetDO.getLanguage().toLowerCase() : null);
		
		Edge publishedBy = VertexHelper.createOrUpdateEdge(session, true, SocialGraphConstants.PUBLISHES_EDGE_LABEL,
				userVertex, tweetVertex);
		
		for (String term : tweetDO.getHashtags()) {
			Vertex termVertex = VertexHelper.getVertexByLabelAndPropertyKey(session,
					SocialGraphConstants.TERM_VERTEX_LABEL + suffix, 
					SocialGraphConstants.NAME_PROPERTY_KEY, term.toLowerCase());
			if (termVertex == null) {
				termVertex = VertexHelper.createVertex(session,
					SocialGraphConstants.TERM_VERTEX_LABEL + suffix,
					SocialGraphConstants.NAME_PROPERTY_KEY, term.toLowerCase(),
					SocialGraphConstants.TYPE_PROPERTY_KEY, "hashtag");
			}
			
			Edge replyToEdge = VertexHelper.createOrUpdateEdge(session, true, SocialGraphConstants.USES_EDGE_LABEL,
					tweetVertex, termVertex);
		}
		
	}
	
	public void createSinlgeWithCache(Tweet tweetDO, boolean useCustomVertexId) {
		String suffix = (useCustomVertexId ? "CV" : "");

		Vertex userVertex = getFromCache(SocialGraphConstants.TWITTER_USER_VERTEX_LABEL, tweetDO.getUser().getId());
		if (userVertex == null) {
			userVertex = VertexHelper.getVertexByLabelAndPropertyKey(session,
					SocialGraphConstants.TWITTER_USER_VERTEX_LABEL + suffix, 
					SocialGraphConstants.ID_PROPERTY_KEY, tweetDO.getUser().getId());
			if (userVertex == null) {
				userVertex = VertexHelper.createVertex(session,
					SocialGraphConstants.TWITTER_USER_VERTEX_LABEL + suffix, 
					SocialGraphConstants.ID_PROPERTY_KEY, tweetDO.getUser().getId(), 
					SocialGraphConstants.NAME_PROPERTY_KEY, tweetDO.getUser().getScreenName() != null ? tweetDO.getUser().getScreenName().toLowerCase() : null,
					SocialGraphConstants.LANGUAGE_PROPERTY_KEY, tweetDO.getUser().getLanguage() != null ? tweetDO.getUser().getLanguage().toLowerCase() : null,
					SocialGraphConstants.VERIFIED_PROPERTY_KEY, tweetDO.getUser().getVerified() != null ? tweetDO.getUser().getVerified() : false);
			}
			addToCache(SocialGraphConstants.TWITTER_USER_VERTEX_LABEL + suffix, tweetDO.getUser().getId(), userVertex);
		} 
		
		Vertex tweetVertex = VertexHelper.createVertex(session,
				SocialGraphConstants.TWEET_VERTEX_LABEL + suffix, 
				SocialGraphConstants.ID_PROPERTY_KEY, tweetDO.getId(), 
				SocialGraphConstants.TIME_PROPERTY_KEY, tweetDO.getCreatedAt().toDate().getTime(), 
				SocialGraphConstants.LANGUAGE_PROPERTY_KEY, tweetDO.getLanguage() != null ? tweetDO.getLanguage().toLowerCase() : null);
		
		Edge publishedBy = VertexHelper.createOrUpdateEdge(session, true, SocialGraphConstants.PUBLISHES_EDGE_LABEL,
				userVertex, tweetVertex);
		
		for (String term : tweetDO.getHashtags()) {
			Vertex termVertex = getFromCache(SocialGraphConstants.TERM_VERTEX_LABEL + suffix, term.toLowerCase());
			if (termVertex == null) {
				termVertex = VertexHelper.getVertexByLabelAndPropertyKey(session,
						SocialGraphConstants.TERM_VERTEX_LABEL + suffix, 
						SocialGraphConstants.NAME_PROPERTY_KEY, term.toLowerCase());
				if (termVertex == null) {
					termVertex = VertexHelper.createVertex(session,
							SocialGraphConstants.TERM_VERTEX_LABEL + suffix,
							SocialGraphConstants.NAME_PROPERTY_KEY, term.toLowerCase(),
							SocialGraphConstants.TYPE_PROPERTY_KEY, "hashtag");
				}
				addToCache(SocialGraphConstants.TERM_VERTEX_LABEL + suffix, term.toLowerCase(), termVertex);
			}
			
			Edge replyToEdge = VertexHelper.createOrUpdateEdge(session, true, SocialGraphConstants.USES_EDGE_LABEL,
					tweetVertex, termVertex);
		}
		
	}
	
	public GraphMetrics createScripted(Tweet tweetDO, boolean useCustomVertexId) {
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
