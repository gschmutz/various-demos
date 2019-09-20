package com.thyssenkrupp.tkse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.avro.specific.SpecificRecord;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import com.thyssenkrupp.tkse.avro.geoevent.v1.GeoEvent;
import com.thyssenkrupp.tkse.avro.geoevent.v1.MatchedGeoFence;
import com.thyssenkrupp.tkse.avro.geoevent.v1.PositionWithMatchedGeoFences;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class KafkaStreamsGeoEventApp {

	static final String MATCHED_FENCE_STREAM = "tkL4.barge_position_matched_geofences";
	static final String GEO_EVENT_STREAM = "tkL4.barge_geo_event";
	
	private static <VT extends SpecificRecord> SpecificAvroSerde<VT> createSerde(String schemaRegistryUrl) {
		SpecificAvroSerde<VT> serde = new SpecificAvroSerde<>();
		Map<String, String> serdeConfig = Collections
				.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
		serde.configure(serdeConfig, false);
		return serde;
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


		final SpecificAvroSerde<PositionWithMatchedGeoFences> positionWithMatchedGeoFencesSerde = createSerde(schemaRegistryUrl);
		
		final StreamsBuilder builder = new StreamsBuilder();

		
		// read the stream from the matched_geofences topic 
		final KStream<String, PositionWithMatchedGeoFences> positionWithMatchedGeoFences = builder.stream(MATCHED_FENCE_STREAM);
		
		// geo_event handling
		final StoreBuilder<KeyValueStore<String, PositionWithMatchedGeoFences>> bargeGeoFenceStatusStore = Stores
				.keyValueStoreBuilder(Stores.persistentKeyValueStore("GeoFenceSnapshotStore"), Serdes.String(), positionWithMatchedGeoFencesSerde)
				.withCachingEnabled();
		builder.addStateStore(bargeGeoFenceStatusStore);

		KStream<String, List<GeoEvent>> geoEvents = positionWithMatchedGeoFences.transformValues(() -> new CommandHandler(bargeGeoFenceStatusStore.name()), bargeGeoFenceStatusStore.name());
		KStream<String, GeoEvent> geoEvent = geoEvents.flatMapValues(v -> v);
		KStream<String, GeoEvent> geoEventByBargeId = geoEvent.selectKey((k, v) -> v.getBargeId().toString());
		geoEventByBargeId.to(GEO_EVENT_STREAM);
		
		return new KafkaStreams(builder.build(), streamsConfiguration);
	}
	
	private static final class CommandHandler implements ValueTransformer<PositionWithMatchedGeoFences, List<GeoEvent>> {
		final private String storeName;
	    private KeyValueStore<String, PositionWithMatchedGeoFences> stateStore;
	    private ProcessorContext context;	    
	    
		public CommandHandler(final String storeName) {
	        Objects.requireNonNull(storeName,"Store Name can't be null");
			this.storeName = storeName;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void init(ProcessorContext context) {
			this.context = context;
	        stateStore = (KeyValueStore<String, PositionWithMatchedGeoFences>) this.context.getStateStore(storeName);			
		}

		@Override
		public List<GeoEvent> transform(PositionWithMatchedGeoFences positionWithMatchedGeoFences) {
	
			String key = positionWithMatchedGeoFences.getBargeId().toString();

			// if first time, then init with current and will do nothing on next if
			if (stateStore.get(key) == null) {
				stateStore.put(key, new PositionWithMatchedGeoFences(null, null, null, null, null, Collections.emptyList()));
			} 
			
			List<MatchedGeoFence> currentFences = positionWithMatchedGeoFences.getMatchedGeoFences();
			List<MatchedGeoFence> oldFences = stateStore.get(key).getMatchedGeoFences();
			
			List<MatchedGeoFence> allLeft = new ArrayList<MatchedGeoFence>(oldFences);
			allLeft.removeAll(currentFences);
			
			List<MatchedGeoFence> allEntered = new ArrayList<MatchedGeoFence>(currentFences);
			allEntered.removeAll(oldFences);
			
			stateStore.put(key, positionWithMatchedGeoFences);
			
			List<GeoEvent> geoEvents = new ArrayList<GeoEvent>();
			
			// TODO "LEAVE" and "ENTER" as constants
			allLeft.forEach(f -> geoEvents.add(new GeoEvent(positionWithMatchedGeoFences.getBargeId(), "LEAVE", positionWithMatchedGeoFences.getEventTime(), f.getId(), f.getShortName())));
			allEntered.forEach(f -> geoEvents.add(new GeoEvent(positionWithMatchedGeoFences.getBargeId(), "ENTER", positionWithMatchedGeoFences.getEventTime(), f.getId(), f.getShortName())));
			
			return geoEvents;
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			
		}

	}


}
