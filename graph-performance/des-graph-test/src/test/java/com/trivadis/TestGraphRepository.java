package com.trivadis;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphNode;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.trivadis.domain.Tweet;
import com.trivadis.domain.User;


public class TestGraphRepository {
	
	Properties properties = new Properties();
	
	private String cassandraHost;
	private String cassandraPort;

	private static final String GRAPH_NAME = "test_v10";
	private static final int NOF_RUNS = 10;
	
	private GraphRepository repoSingle = null;
	private GraphRepository repoScript = null;
	private DseSession session = null;
	
	private DateTime createDate(String dateTime) {
		DateTime result = new DateTime();
		return result;
	}
	
	private void dropData() {
		session.executeGraph("g.V().drop().iterate()");
	}

	private void printCount() {
		session.executeGraph("schema.config().option('graph.allow_scan').set('true')");
		GraphResultSet result = session.executeGraph("g.V().groupCount().by(label)");
		for (GraphNode node : result.all()) {
			System.out.println(node.asString());
		}
		session.executeGraph("schema.config().option('graph.allow_scan').set('false')");
	}
	
	private void reportResult(int currentRun, int nofRuns, int nofTweets, int nofHashtags, long elapsedTime) {
		System.out.println("[" + (currentRun+1) + " out of " + nofRuns + "] took " + elapsedTime + " ms.");
	}

	private List<String> createHashtags(String... hashtags) {
		List<String> hts = new ArrayList<String>();

		for (String hashtag : hashtags) {
			hts.add(hashtag);
		}

		return hts;
	}

	private List<Tweet> createTweets(int nofTweets, int nofHashtags) {
		List<Tweet> tweets = new ArrayList<Tweet>();
		List<String> hashtags = new ArrayList<String>();
	
		for (int i = 0; i < nofHashtags; i++) {
			hashtags.add("hashtag-" + i);
		}
		
		for (int i = 0; i < nofTweets; i++) {
			User user = new User(Long.valueOf(i), String.valueOf(i), new DateTime(), "en", "user" + i, true);
			Tweet tweet = new Tweet(Long.valueOf(i), new DateTime(), createHashtags(hashtags.toArray(new String[0])), null, null, user, "en");
			tweets.add(tweet);
		}
		return tweets;
	}
	
	private GraphMetrics save(boolean single, Tweet tweetDO) {
		GraphMetrics result = null;
		if (single) {
			result = repoSingle.createTweetAndUsers(tweetDO);
		} else {
			result = repoScript.createTweetAndUsers(tweetDO);
		}
		return result;
	}
	
	private void doTest(boolean single, int nofRuns, int nofTweets, int nofHashtags) {
		System.out.println("Starting " + (single ? "GraphRepositorySingle" : "GraphRepositoryGroovyScript") + " Test runs adding " + nofTweets + " with " + nofHashtags + " hashtags each.");
		long startTime = 0;
		List<Tweet> tweets = createTweets(nofTweets,nofHashtags);

		// loop nofRuns
		for (int r = 0; r < nofRuns; r++) {
			startTime = System.currentTimeMillis();
			
			// loop for each tweet
			for (Tweet tweet : tweets) {
				GraphMetrics gm = (GraphMetrics) save(single, tweet);
			}
			
			long elapsedTime = System.currentTimeMillis() - startTime;
			reportResult(r, nofRuns, nofTweets, nofHashtags, elapsedTime);
			
			if (r+1 == nofRuns) printCount();
			dropData();
		}

	}
	
	@Before
	public void setup() throws Exception {
		properties.load(TestGraphIngestStrategies.class.getClassLoader().getResourceAsStream("config.properties"));
		cassandraHost = properties.getProperty("cassandra.host");
		cassandraPort = properties.getProperty("cassandra.port");
		System.out.println("Running Tests against " + cassandraHost + "......");
		
		repoScript = new GraphRepositoryGroovyScript(cassandraHost, cassandraPort, GRAPH_NAME);
		repoSingle = new GraphRepositorySingle(cassandraHost, cassandraPort, GRAPH_NAME);
		session = ((GraphRepositorySingle)repoSingle).getDseSession();
		dropData();
	}
	
	@After
	public void teardown() {
		session.close();
	}

	@Test
	public void testCreateTweets1_2() {
		// run test for single
//		doTest(true, NOF_RUNS, 1, 2);
		
		// run test for script
		//doTest(false, NOF_RUNS, 1, 2);
	}

	@Test
	public void testCreateTweets10_10() {
		// run test for single
		doTest(true, NOF_RUNS, 10, 10);
		
		// run test for script
		doTest(false, NOF_RUNS, 10, 10);
	}

	@Test
	public void testCreateTweets10_20() {
		// run test for single
		doTest(true, NOF_RUNS, 10, 20);
		
		// run test for script
		doTest(false, NOF_RUNS, 10, 20);
	}

	@Test
	public void testCreateTweets10_30() {
		// run test for single
		doTest(true, NOF_RUNS, 10, 30);
		
		// run test for script
		doTest(false, NOF_RUNS, 10, 30);
	}

	@Test
	public void testCreateTweets20_30() {
		// run test for single
		doTest(true, NOF_RUNS, 20, 30);
		
		// run test for script
		doTest(false, NOF_RUNS, 20, 30);
	}

	@Test
	public void testCreateTweets20_40() {
		// run test for single
		doTest(true, NOF_RUNS, 20, 40);
		
		// run test for script
		doTest(false, NOF_RUNS, 20, 40);
	}

	@Test
	public void testCreateTweets100_10() {
		// run test for single
		doTest(true, NOF_RUNS, 100, 10);
		
		// run test for script
		doTest(false, NOF_RUNS, 100, 10);
	}
}
