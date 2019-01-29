package com.trivadis;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphNode;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.trivadis.domain.Tweet;
import com.trivadis.domain.User;

public class TestGraphIngestStrategies {
	
	Properties properties = new Properties();
	
	private String cassandraHost;
	private String cassandraPort;

	private static final String GRAPH_NAME = "test_v10";
	private static final int NOF_RUNS = 10;
	private static final int READ_TIMEOUT = 0;

	private DseSession session = null;
	
	private GraphIngestStrategies repo = null;
	
	private DateTime createDate(String dateTime) {
		DateTime result = new DateTime();
		return result;
	}
	
	private void executeStmt(String stmt) {
		session.executeGraph(stmt);
	}
	
	private void dropData() {
		session.executeGraph("schema.config().option('graph.allow_scan').set('true')");
		session.executeGraph("g.V().drop().iterate()");
		session.executeGraph("schema.config().option('graph.allow_scan').set('false')");
	}
	
	private void printCount() {
		session.executeGraph("schema.config().option('graph.allow_scan').set('true')");
		GraphResultSet result = session.executeGraph("g.V().groupCount().by(label)");
		for (GraphNode node : result.all()) {
			System.out.println(node.toString());
		}
		session.executeGraph("schema.config().option('graph.allow_scan').set('false')");
	}
	
	private void reportResult(int currentRun, int nofRuns, int nofTweets, int nofHashtags, double elapsedTime) {
		System.out.println("[" + (currentRun+1) + " out of " + nofRuns + "] took " + new Double(elapsedTime).intValue() + " ms." + "cacheHit: " + repo.cacheHit + ", cacheMiss: " + repo.cacheMiss);
	}

	private List<String> createHashtags(String... hashtags) {
		List<String> hts = new ArrayList<String>();

		for (String hashtag : hashtags) {
			hts.add(hashtag);
		}

		return hts;
	}
	
	private List<Tweet> createTweets(int nofTweets, int nofHashtags, int hashtagBound, int userBound) {
		Random r = new Random(233232);
		List<Tweet> tweets = new ArrayList<Tweet>();
		
		for (int i = 0; i < nofTweets; i++) {
			List<String> hashtags = new ArrayList<String>();
			for (int h = 0; h < nofHashtags; h++) {
				hashtags.add("hashtag-" + i + r.nextInt(hashtagBound));
			}

			int userId = r.nextInt(userBound);
			User user = new User(Long.valueOf(userId), String.valueOf(userId), new DateTime(), "en", "user" + (userId), true);
			Tweet tweet = new Tweet(Long.valueOf(i), new DateTime(), createHashtags(hashtags.toArray(new String[0])), null, null, user, "en");
			tweets.add(tweet);
		}
		return tweets;
	}	
	
	private void doTest(int mode, boolean withCustomVertexId, int nofRuns, int nofTweets, int nofHashtags, int hashtagBound, int userBound) {

		System.out.println("Starting " + "GraphIntestStrategies [" + ((mode==1) ? "withCache" : ((mode==2) ? "noCache" : "scripted")) + "," + (withCustomVertexId ? "withCustomVertexId" : "withGeneratedVertexId") + "," + hashtagBound + "," + userBound +"]: Test runs adding " + nofTweets + " with " + nofHashtags + " hashtags each.");
		long startTime = 0;
		List<Tweet> tweets = createTweets(nofTweets,nofHashtags, hashtagBound, userBound);
		double[] elapsedTimes = new double[nofRuns];
		// loop nofRuns
		for (int r = 0; r < nofRuns; r++) {
			repo.clearCache();

			startTime = System.currentTimeMillis();
			
			// loop for each tweet
			for (Tweet tweet : tweets) {
				if (mode == 1) {
					if (withCustomVertexId)
						repo.createSinlgeWithCache(tweet, true);
					else
						repo.createSinlgeWithCache(tweet, false);
				} else if (mode == 2) {
					if (withCustomVertexId)
						repo.createSingle(tweet, true);
					else
						repo.createSingle(tweet, false);
				} else if (mode == 3) {
					if (withCustomVertexId)
						repo.createScripted(tweet, true);
					else
						repo.createScripted(tweet, false);
				}
			}
			
			elapsedTimes[r] = System.currentTimeMillis() - startTime;
			reportResult(r, nofRuns, nofTweets, nofHashtags, elapsedTimes[r]);
			
			if (r+1 == nofRuns) printCount();
			dropData();
		}
		
		System.out.println("Median: " + StatUtils.percentile(elapsedTimes, 50) + ", 10th p: " +  StatUtils.percentile(elapsedTimes, 10) + ", 90th p: " + StatUtils.percentile(elapsedTimes, 90));
	}

	@Before
	public void setup() throws Exception {
		properties.load(TestGraphIngestStrategies.class.getClassLoader().getResourceAsStream("config.properties"));
		cassandraHost = properties.getProperty("cassandra.host");
		cassandraPort = properties.getProperty("cassandra.port");
		System.out.println("Running Tests against " + cassandraHost + "......");
		DseCluster dseCluster = DseCluster.builder()
		        .addContactPoints(StringUtils.split(cassandraHost,","))
		        .withGraphOptions(new GraphOptions().setGraphName(GRAPH_NAME)
						.setReadTimeoutMillis(0)
						.setGraphSource("g")
						.setGraphReadConsistencyLevel(ConsistencyLevel.ONE)
						.setGraphWriteConsistencyLevel(ConsistencyLevel.ONE))
		        .build();
		session = dseCluster.connect();
		repo = new GraphIngestStrategies(cassandraHost, cassandraPort, GRAPH_NAME);
		dropData();
	}
	
	@After
	public void teardown() {
		session.close();
	}

	// With Generated Vertex IDs
	
	@Test
	public void testCreateTweets10_10() {
		int hashtagBound = 10;
		int userBound = 10;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 10, 10, hashtagBound, userBound);

		hashtagBound = 5;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 10, 10, hashtagBound, userBound);

		hashtagBound = 1;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 10, 10, hashtagBound, userBound);
	}

/*
	@Test
	public void testCreateTweets40_10_20_20() {
		int hashtagBound = 20;
		int userBound = 20;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 40, 10, hashtagBound, userBound);

		// run with cache and custom Vertex ID
		doTest(1, true, NOF_RUNS, 40, 10, hashtagBound, userBound);

		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 40, 10, hashtagBound, userBound);

		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 40, 10, hashtagBound, userBound);
	}
*/
	@Test
	public void testCreateTweets40_10() {
		int hashtagBound = 10;
		int userBound = 10;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 40, 10, hashtagBound, userBound);

		hashtagBound = 5;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 40, 10, hashtagBound, userBound);

		hashtagBound = 1;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 40, 10, hashtagBound, userBound);

	}

	@Test
	public void testCreateTweets60_10() {
		int hashtagBound = 10;
		int userBound = 10;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 60, 10, hashtagBound, userBound);

		hashtagBound = 5;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		hashtagBound = 1;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 60, 10, hashtagBound, userBound);

	}

	@Test
	public void testCreateTweets20_40() {

		int hashtagBound = 10;
		int userBound = 10;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 20, 40, hashtagBound, userBound);

		hashtagBound = 5;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 20, 40, hashtagBound, userBound);

		hashtagBound = 1;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 20, 40, hashtagBound, userBound);
	}


	
	// --------------------------------------------------------------------------------
	// With Custom Vertex IDs
	// --------------------------------------------------------------------------------

	@Test
	public void testCreateTweetsCV10_10() {
		int hashtagBound = 10;
		int userBound = 10;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 10, 10, hashtagBound, userBound);

		hashtagBound = 5;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 10, 10, hashtagBound, userBound);

		hashtagBound = 1;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 10, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 10, 10, hashtagBound, userBound);
	}

/*
	@Test
	public void testCreateTweets40_10_20_20() {
		int hashtagBound = 20;
		int userBound = 20;
		// run with cache and generated Vertex ID
		doTest(1, false, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, false, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, false, NOF_RUNS, 40, 10, hashtagBound, userBound);

		// run with cache and custom Vertex ID
		doTest(1, true, NOF_RUNS, 40, 10, hashtagBound, userBound);

		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 40, 10, hashtagBound, userBound);

		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 40, 10, hashtagBound, userBound);
	}
*/
	@Test
	public void testCreateTweetsCV40_10() {
		int hashtagBound = 10;
		int userBound = 10;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 40, 10, hashtagBound, userBound);

		hashtagBound = 5;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 40, 10, hashtagBound, userBound);

		hashtagBound = 1;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 40, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 40, 10, hashtagBound, userBound);
	}


	@Test
	public void testCreateTweetsCV60_10() {
		int hashtagBound = 10;
		int userBound = 10;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 60, 10, hashtagBound, userBound);

		hashtagBound = 5;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 60, 10, hashtagBound, userBound);

		hashtagBound = 1;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 60, 10, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 60, 10, hashtagBound, userBound);

	}

	@Test
	public void testCreateTweetsCV20_40() {
		int hashtagBound = 10;
		int userBound = 10;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 20, 40, hashtagBound, userBound);

		hashtagBound = 5;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 20, 40, hashtagBound, userBound);

		hashtagBound = 1;
		userBound = 5;
		// run with cache and generated Vertex ID
		doTest(1, true, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run without cache and generated Vertex ID
		doTest(2, true, NOF_RUNS, 20, 40, hashtagBound, userBound);
		
		// run scripted and generated Vertex ID
		doTest(3, true, NOF_RUNS, 20, 40, hashtagBound, userBound);
	}


}
