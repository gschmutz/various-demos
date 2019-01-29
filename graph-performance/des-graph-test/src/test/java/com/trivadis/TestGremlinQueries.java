package com.trivadis;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

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
import com.datastax.driver.dse.graph.SimpleGraphStatement;
import com.google.common.util.concurrent.ListenableFuture;


public class TestGremlinQueries {
	
	Properties properties = new Properties();
	
	private String cassandraHost;
	private String cassandraPort;

	private static final String GRAPH_NAME = "sma_graph_22mar_prod_v10";
	private static final int NOF_RUNS = 20;
	private static final int READ_TIMEOUT = 0;
	
	private DseSession session = null;
	
	private DateTime createDate(String dateTime) {
		DateTime result = new DateTime();
		return result;
	}
	
	private void executeStmt(String stmt) {
		session.executeGraph(stmt);
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
		
	private void reportResult(int currentRun, int nofRuns, int nofResults, double elapsedTime) {
		System.out.println("[" + (currentRun+1) + " out of " + nofRuns + "] returned " + nofResults + " results and took " + new Double(elapsedTime).intValue() + " ms.");
	}
	
	
	private void executeGremlin(String stmt, String testCase, int nofRuns) throws InterruptedException, ExecutionException {
		double[] elapsedTimes = new double[nofRuns];

		System.out.println("======================================================");
		System.out.println("Executing .... (" + testCase + ")"); 
		System.out.println(stmt);
		long startTime = 0;

		// loop nofRuns
		for (int r = 0; r < nofRuns; r++) {
			startTime = System.currentTimeMillis();
			
			// loop for each tweet
			SimpleGraphStatement sgs = new SimpleGraphStatement(stmt);

			ListenableFuture<GraphResultSet> f = session.executeGraphAsync(sgs);
			GraphResultSet grs = f.get();
			List<GraphNode> l = grs.all();
			elapsedTimes[r] = System.currentTimeMillis() - startTime;
			reportResult(r, nofRuns, l.size(), elapsedTimes[r]);
			
		}
		System.out.println("Median: " + StatUtils.percentile(elapsedTimes, 50) + ", 10th p: " +  StatUtils.percentile(elapsedTimes, 10) + ", 90th p: " + StatUtils.percentile(elapsedTimes, 90));

	}
	
	@Before
	public void setup() throws Exception {
		properties.load(TestGraphIngestStrategies.class.getClassLoader().getResourceAsStream("config.properties"));
		cassandraHost = properties.getProperty("cassandra.host");
		cassandraPort = properties.getProperty("cassandra.port");
		String graphSource = properties.getProperty("graph.source");
		
		System.out.println("Running Tests against " + cassandraHost + "......");

		DseCluster dseCluster = DseCluster.builder()
		        .addContactPoints(StringUtils.split(cassandraHost,","))
		        .withGraphOptions(new GraphOptions().setGraphName(GRAPH_NAME)
						.setReadTimeoutMillis(0)
						.setGraphSource(graphSource)
						.setGraphReadConsistencyLevel(ConsistencyLevel.ONE)
						.setGraphWriteConsistencyLevel(ConsistencyLevel.ONE))
		        .build();
		session = dseCluster.connect();
	}
	
	@After
	public void teardown() {
		session.close();
	}

	@Test
	public void testUseCase_1() throws InterruptedException, ExecutionException {
		String tc = "testUseCase_1"; 
		String stmt = "g.V().has('tweet', 'id', 712172172748304384)." + "\n"
				+ "in('publishes')." + "\n"
				+ "out('publishes')."  + "\n"
				+ "has('id', neq(712172172748304384))."  + "\n"
				+ "in('retweets')."  + "\n"
				+ "in('publishes')."  + "\n"
				+ "out('publishes').count()";
		executeGremlin(stmt, tc, NOF_RUNS);
	}

	//@Test
	public void testUseCase_2() throws InterruptedException, ExecutionException {
		String tc = "testUseCase_2"; 
		String stmt = "g.V().has('tweet', 'id', '712172172748304384')." + "\n"
					+ "repeat(both()." + "\n"
					+ "simplePath())." + "\n"
					+ "until(has('id', '712193920411832321'))." + "\n"
					+ "path().limit(1)";
		executeGremlin(stmt, tc, NOF_RUNS);
	}
	
	@Test
	public void testUseCase_3() throws InterruptedException, ExecutionException {
		String tc = "testUseCase_3"; 
		String stmt = "g.V().has('twitterUser','name',Search.prefix('cnn'))";
		executeGremlin(stmt, tc, NOF_RUNS);
	}
	
	@Test
	public void testUseCase_4() throws InterruptedException, ExecutionException {
		String tc = "testUseCase_4"; 
		String stmt = "g.V().has('term','name','bomb').in('contains')";
		executeGremlin(stmt, tc, NOF_RUNS);
	}


	@Test
	public void testUseCase_4b() throws InterruptedException, ExecutionException {
		String tc = "testUseCase_4b (returning text only)"; 
		String stmt = "g.V().has('term','name','bomb').in('contains').values('text')";
		executeGremlin(stmt, tc, NOF_RUNS);
	}

	@Test
	public void testUseCase_4c() throws InterruptedException, ExecutionException {
		String tc = "testUseCase_4c (without index)"; 
		executeStmt("schema.vertexLabel('tweet').index('containsType').remove()");

		String stmt = "g.V().has('term','name','bomb').inE('contains').has('type','hashtag').outV()";
		executeGremlin(stmt, tc, NOF_RUNS);

	}
	
	@Test
	public void testUseCase_4d() throws InterruptedException, ExecutionException {
		String tc = "testUseCase_4d - (with Index)"; 
		executeStmt("schema.vertexLabel('tweet').index('containsType').outE('contains').by('type').ifNotExists().add()");

		String stmt = "g.V().has('term','name','bomb').inE('contains').has('type','hashtag').outV()";
		executeGremlin(stmt, tc, NOF_RUNS);

		executeStmt("schema.vertexLabel('tweet').index('containsType').remove()");
	}
	@Test
	public void testUseCase_5() throws InterruptedException, ExecutionException {
		String tc = "testUseCase_5"; 
		String stmt = "g.V().has('term','name','bomb')." + "\n" + 
						"in('contains').as('t1')." + "\n" + 
						"in('publishes').as('u')." + "\n" + 
						"out('publishes').where(neq('t1')).as('t2')." + "\n" + 
						"out('contains').has('name','shock')." + "\n" + 
						"select('u', 't2').by('name').by('text')";
		executeGremlin(stmt, tc, NOF_RUNS);
	}
	
	@Test
	public void testUseCase_6a() throws InterruptedException, ExecutionException {
		String tc = "testUseCase_6"; 
		String stmt = "g.V().has('term','name','bomb')." + "\n" + 
						"in('contains').as('t1')." + "\n" + 
						"in('publishes').as('u')." + "\n" + 
						"out('publishes').where(neq('t1')).as('t2')." + "\n" + 
						"outE('contains').has('type','hashtag')." + "\n" + 
						"inV().has('name','maga')." + "\n" + 
						"select('u', 't2').by('name').by('text')";
		executeGremlin(stmt, tc, NOF_RUNS);
	}	
	
	@Test
	public void testUseCase_6b() throws InterruptedException, ExecutionException {
		String tc = "testUseCase_6b"; 
		executeStmt("schema.vertexLabel('tweet').index('containsType').outE('contains').by('type').ifNotExists().add()");
		
		String stmt = "g.V().has('term','name','bomb')." + "\n" + 
						"in('contains').as('t1')." + "\n" + 
						"in('publishes').as('u')." + "\n" + 
						"out('publishes').where(neq('t1')).as('t2')." + "\n" + 
						"outE('contains').has('type','hashtag')." + "\n" + 
						"inV().has('name','maga')." + "\n" + 
						"select('u', 't2').by('name').by('text')";
		executeGremlin(stmt, tc, NOF_RUNS);

		executeStmt("schema.vertexLabel('tweet').index('containsType').remove()");
	}		
}
