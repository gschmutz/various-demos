package com.thyssenkrupp.tkse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.avro.specific.SpecificRecord;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import com.thyssenkrupp.tkse.avro.geofences.v1.GeoFence;
import com.thyssenkrupp.tkse.avro.geofences.v1.GeoFenceItem;
import com.thyssenkrupp.tkse.avro.geofences.v1.GeoFenceList;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class KafkaStreamsGeoFencesApp {

	static final String GEO_FENCES_KEYEDBY_GEOHASH = "geo_fences_keyedby_geohash";
	static final String GEO_FENCE = "geo_fence";

	private static <VT extends SpecificRecord> SpecificAvroSerde<VT> createSerde(String schemaRegistryUrl) {
		SpecificAvroSerde<VT> serde = new SpecificAvroSerde<>();
		Map<String, String> serdeConfig = Collections
				.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
		serde.configure(serdeConfig, false);
		return serde;
	}

	private static GeoFence createFrom(GeoFence geoFence, String geoHash) {
		GeoFence geoFencesByGeoHash = new GeoFence(
									geoFence.getId(),
									geoFence.getShortName(),
									geoFence.getLongName(),
									geoFence.getWkt(),
									geoFence.getTyp(),
									geoHash);

		return geoFencesByGeoHash;
	}
	
	public static void main(final String[] args) {
		String applicationId = null;
		String clientId = null;
		String bootstrapServer = "localhost:9092";
		String schemaRegistryUrl = "http://localhost:8081";
		boolean cleanup = false;
		
		// create the command line parser
		CommandLineParser parser = new DefaultParser();

		// create the Options
		Options options = new Options();
		options.addOption( "ai", "application-id", true, "REQUIRED: The application id." );
		options.addOption( "ci", "client-id", true, "The client id. Defaults to the application id + the prefix '-client'" );
		options.addOption( "b", "bootstrap-server", true, "The server(s) to connect to, default to " + bootstrapServer);
		options.addOption( "sr", "schemaRegistryUrl", true, "The schema registry to connect to for the Avro schemas, defaults to " + schemaRegistryUrl );
		options.addOption( "cl", "cleanup", false, "Should a cleanup be performed before staring. Defaults to false" );

		try {
		    // parse the command line arguments
		    CommandLine line = parser.parse( options, args );

		    if( line.hasOption( "application-id" ) ) {
		        applicationId = line.getOptionValue("application-id");
		    } 
		    if( line.hasOption( "client-id" ) ) {
		        clientId = line.getOptionValue("client-id");
		    } else {
		    	clientId = applicationId + "-client";
		    }
		    	
		    if( line.hasOption( "bootstrap-server" ) ) {
		        bootstrapServer = line.getOptionValue("bootstrap-server");
		    } 
		    if( line.hasOption( "schemaRegistryUrl" ) ) {
		    	schemaRegistryUrl = line.getOptionValue("schemaRegistryUrl");
		    }
		    if( line.hasOption( "cleanup" ) ) {
		    	cleanup = true;
		    }
		}
		catch( ParseException exp ) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "kafka-passthrough", exp.getMessage(), options,null, true);
		}		
		final KafkaStreams streams = buildFeed(applicationId, clientId, bootstrapServer, schemaRegistryUrl, "/tmp/kafka-streams");

		if (cleanup) {
			streams.cleanUp();
		}
		streams.start();

		// Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				streams.close();
			}
		}));
	}

	private static KafkaStreams buildFeed(final String applicationId, final String clientId, final String bootstrapServers, final String schemaRegistryUrl,
			final String stateDir) {

		final Properties streamsConfiguration = new Properties();

		// Give the Streams application a unique name. The name must be unique in the
		// Kafka cluster
		// against which the application is run.
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, clientId);

		// Where to find Kafka broker(s).
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		// Where to find the Confluent schema registry instance(s)
		streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

		// Specify default (de)serializers for record keys and for record values.
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
		streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
		streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		// Records should be flushed every 10 seconds. This is less than the default
		// in order to keep this example interactive.
		streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);

		// If Confluent monitoring interceptors are on the classpath,
		// then the producer and consumer interceptors are added to the
		// streams application.
		// MonitoringInterceptorUtils.maybeConfigureInterceptorsStreams(streamsConfiguration);

		final Serde<String> stringSerde = Serdes.String();

		final StreamsBuilder builder = new StreamsBuilder();

		// get the stream of GeoFences
		KStream<String, GeoFence> geoFence = builder.stream(GEO_FENCE);
		
		// retrieve the an array of geohash for the covering bounding box around the geofence and flat map it so that we get a new tuple with key=geohash and value=GeoFencesByGeoHash 
		// TODO: create a constant for the GeoHash lenght (5)
		KStream<String, GeoFence> geoFenceByGeoHash = geoFence.map((k,v) -> KeyValue.<GeoFence, List<String>> pair(v, GeoHashUtil.coverBoundingBox(v.getWkt().toString(), 5)))
															.flatMapValues(v -> v)
															.map((k,v) -> KeyValue.<String,GeoFence>pair(v, createFrom(k, v)));
		
		//geoFencesByGeoHash.peek((k, v) -> System.out.println (k + ":" +v));
		
		KTable<String, GeoFenceList> geofencesByGeohash = geoFenceByGeoHash.groupByKey().aggregate(
				() -> new GeoFenceList(new ArrayList<GeoFenceItem>()), 	// initializer
				(aggKey, newValue, aggValue) -> {
					GeoFenceItem geoFenceItem = new GeoFenceItem(newValue.getId(), newValue.getShortName(), newValue.getLongName(), newValue.getWkt(), newValue.getTyp());
					if (!aggValue.getGeoFences().contains(geoFenceItem))
						aggValue.getGeoFences().add(geoFenceItem);
					return aggValue;
				},
				Materialized.<String, GeoFenceList, KeyValueStore<Bytes,byte[]>>as("geofences-by-geohash-store"));
		
		// produce to the topic
		geofencesByGeohash.toStream().to(GEO_FENCES_KEYEDBY_GEOHASH, Produced.<String, GeoFenceList> keySerde(stringSerde));										
										
		return new KafkaStreams(builder.build(), streamsConfiguration);
	}

}
