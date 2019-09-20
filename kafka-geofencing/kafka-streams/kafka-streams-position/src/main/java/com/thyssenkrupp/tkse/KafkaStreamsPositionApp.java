package com.thyssenkrupp.tkse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import com.trivadis.demo.avro.geofences.v1.GeoFenceItem;
import com.trivadis.demo.avro.geofences.v1.GeoFenceList;
import com.trivadis.demo.avro.position.v1.VehiclePosition;
import com.trivadis.demo.avro.position.v1.VehiclePositionWithGeohash;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class KafkaStreamsPositionApp {

	static final String VEHICLE_POSITION_STREAM = "vehicle_position";
	static final String VEHICLE_POSITION_WITH_GEOHASH_STREAM = "vehicle_position_with_geohash";
	static final String MATCHED_FENCE = "vehicle_position_matched_geo_fences";
	static final String GEO_FENCES_KEYEDBY_GEOHASH = "geo_fences_keyedby_geohash";
	static final String VEHICLE_STREAM = "vehicle";
	
	private static final String GEOFENCE_WKT_PATH = "geofences.txt";
	private static final GeoFenceList GEOFENCES = new GeoFenceList(new ArrayList<>()); 
	
	static {
		System.out.println("Loading wkt file...");
		InputStream resourceAsStream = RhineUtil.class.getClassLoader().getResourceAsStream(GEOFENCE_WKT_PATH);
		InputStreamReader in = new InputStreamReader(resourceAsStream);
		BufferedReader bufferedReader = new BufferedReader(in);
		String readLine;
		try {
			readLine = bufferedReader.readLine();
			while (readLine != null) {
				String[] split = readLine.split(";");
				GeoFenceItem geo = new GeoFenceItem();
				geo.setName(split[0]);
				geo.setWkt(split[2]);
				geo.setTyp(split[3]);
				GEOFENCES.getGeoFences().add(geo);
				readLine = bufferedReader.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static VehiclePositionWithGeohash createFrom(VehiclePosition position, String geohash) {
		VehiclePositionWithGeohash newPosition = VehiclePositionWithGeohash.newBuilder().setVehicleId(position.getVehicleId())
												.setEventTime(position.getEventTime())
												.setLatitude(position.getLatitude())
												.setLongitude(position.getLongitude())
												.setGeohash(geohash).build();
		return newPosition;
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
		String stateDirPath = "C:\\tmp\\kafka-streams";
		final KafkaStreams streams = buildFeed(applicationId, clientId, bootstrapServer, schemaRegistryUrl, stateDirPath);

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


		final StreamsBuilder builder = new StreamsBuilder();

		// read the source stream (keyed by objectId)
		final KStream<String, VehiclePosition> vehiclePosition = builder.stream(VEHICLE_POSITION_STREAM);
		
	
		// read the barge and group it by objectIdMecomo
		//final KTable<String, Vehicle> vehicle = builder.table(VEHICLE_STREAM);
		
		//barge.toStream().peek((k,v) -> System.out.println("barge.toStream().peek(...) : " + k + " : " + v));
		//positionsMecomoRaw.peek((k,v) -> System.out.println("positionsMecomoRaw.peek(...) : " + k + " : " + v));
		
		// Left Join Positions Mecomo Raw with Barge to get the barge id
		//KStream<String, VehiclePosition> positions  =  positionsMecomoRaw.leftJoin(vehicle, 
		//		(leftValue, rightValue) -> createFrom(leftValue, (rightValue != null ? rightValue.getId() : -1) ),
		//		Joined.<String, PositionMecomoRaw, Barge>keySerde(Serdes.String())
		//		);

		// enrich with GeoHash (the object in the lambda is immutable, always create a new instance => createFrom)
		// TODO: extract constant for the GeoHash lenght
		KStream<String, VehiclePositionWithGeohash> vehiclePositionWithGeoHash = vehiclePosition.mapValues(v -> createFrom(v, GeoHashUtil.geohash(v.getLatitude(), v.getLongitude(), 5) ));
		
		//positionsMecomoWithGeoHash.peek((k,v) -> System.out.println("positionsMecomoWithGeoHash.peek(...) : " + k + " : " + v));
		
		// write Positions with Enrichments (geohash and bargeId, BUT NOT YET eta and direction) to Kafka topic
		vehiclePositionWithGeoHash.selectKey((k, v) -> v.getVehicleId().toString()).to(VEHICLE_POSITION_WITH_GEOHASH_STREAM);

		/*-
		// Read GeoFences keyed by GeoHash into KStream 
		final KStream<String, GeoFence> geofences = builder.stream(GEOFENCE_KEYEDBY_GEOHASH);

		//geofences.peek((k,v) -> System.out.println(k + ":" + v));
		
		// Group the GeoFences by GeoHash and aggregate them in a list of GeoFences by GeoHash (so that it is available for geohash based, partitioned join)
		// as it is not directly supported to have a list as an aggregate value, we use a string and format both id and geofenceWKT into this string 
		// and make it a list. Each entry in the list is formated as: <geofenceId> + '=>' + <geofenceWkt> and the elements are separated by ':'
		
		KTable<String, GeoFenceList> geofencesByGeohash = geofences.groupByKey().aggregate(
				() -> new GeoFenceList(new ArrayList<GeoFenceItem>()), 	// initializer
				(aggKey, newValue, aggValue) -> {
					GeoFenceItem geoFenceItem = new GeoFenceItem(newValue.getId(), newValue.getShortName(), newValue.getLongName(), newValue.getWkt(), newValue.getTyp());
					if (!aggValue.getGeoFences().contains(geoFenceItem))
						aggValue.getGeoFences().add(geoFenceItem);
					return aggValue;
				},
				Materialized.<String, GeoFenceList, KeyValueStore<Bytes,byte[]>>as("geofences-by-geohash-store"));
		 */

		//geofencesByGeohash.toStream().peek((k,v) -> System.out.println(k + ":" +  v));
		
		// partition positions by GeoHash so that they can be joined to the geofences KTable
//		KStream<String, PositionMecomo> positionsMecomoWithGeoHashKeyedByGeoHash = positionsMecomoWithGeoHash.selectKey((id,pos) -> pos.getGeohash().toString());
		KStream<String, VehiclePositionWithGeohash> positionsMecomoWithGeoHashKeyedByBargeId = vehiclePositionWithGeoHash.selectKey((id,pos) -> pos.getBargeId().toString());
		
		//positionsMecomoWithGeoHashKeyedByGeoHash.peek((k, v) -> System.out.println(k + ":" + v));
		
		// Equal Join from Position By GeoHash KStream and GeoFences KTable, returning MatchedFence objects (still keyed by geohash)
		// this is kind of a "hack" in such a way that the MatchedFence uses the WKT to hold a list of fences and not just one (it will be fixed with the next block
		KStream<String, PositionWithMatchedGeoFences> positionWithMatchedGeoFences = positionsMecomoWithGeoHashKeyedByBargeId.mapValues((pos) -> {
			List<MatchedGeoFence> matchedGeofences = new ArrayList<MatchedGeoFence>();
			for (GeoFenceItem geoFenceItem : GEOFENCES.getGeoFences()) {
				boolean geofenceStatus = GeoFenceUtil.geofence(pos.getLat(), pos.getLon(), geoFenceItem.getWkt().toString());
				if(geofenceStatus)
					matchedGeofences.add(new MatchedGeoFence(geoFenceItem.getId(), geoFenceItem.getShortName(), null));
			}
			return new PositionWithMatchedGeoFences(pos.getBargeId(), pos.getPositionId(), pos.getLat(), pos.getLon(), pos.getGpsFixTime(), matchedGeofences);
		});
		
									
		//positionWithMatchedGeoFences.peek((k,v) -> System.out.println(v));
		
		// Send the Matches to the Kafka Topic 
		positionWithMatchedGeoFences.to(MATCHED_FENCE);

		return new KafkaStreams(builder.build(), streamsConfiguration);
	}

}
