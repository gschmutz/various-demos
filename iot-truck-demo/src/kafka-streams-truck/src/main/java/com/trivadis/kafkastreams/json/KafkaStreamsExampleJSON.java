package com.trivadis.kafkastreams.json;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import com.fasterxml.jackson.databind.JsonNode;
import com.trivadis.kafkastreams.Driver;
import com.trivadis.kafkastreams.TruckPosition;

public class KafkaStreamsExampleJSON {
	
	static public class TruckPositionDriver {
		public TruckPosition truckPosition;
		public String driverFirstName;
		public String driverLastname;
		
		public TruckPositionDriver(TruckPosition truckPosition, String driverFirstName, String driverLastname) {
			this.truckPosition = truckPosition;
			this.driverFirstName = driverFirstName;
			this.driverLastname = driverLastname;
		}

		@Override
		public String toString() {
			return "TruckPositionDriver [truckPosition=" + truckPosition + ", driverFirstName=" + driverFirstName
					+ ", driverLastname=" + driverLastname + "]";
		}
	}
	
	public static void main(String[] args) {
		// Serializers/deserializers (serde) for String and Long types
		final Serde<String> stringSerde = Serdes.String();
		final Serde<Long> longSerde = Serdes.Long();
        final Serializer<JsonNode> jsonSerializer = new JsonSerializer();
        final Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
        final Serde<JsonNode> jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
		
	    final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
	    final Properties streamsConfiguration = new Properties();
	    // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
	    // against which the application is run.
	    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-trucking");

	    // Where to find Kafka broker(s).
	    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
	    
	    // Specify default (de)serializers for record keys and for record values.
	    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
	    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());		
	    
	    // specify the TimestampExtrator to use
	    //streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, JsonTimestampExtractor.class);
		
		// In the subsequent lines we define the processing topology of the Streams application.
		// used to be KStreamBuilder ....
	    final StreamsBuilder builder = new StreamsBuilder();

		/*
		 * Prepare serdes to map to/from Json data to Java objects
		 */

		Map<String, Object> serdeProps = new HashMap<>();
		 
        final Serializer<TruckPosition> truckPositionSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", TruckPosition.class);
        truckPositionSerializer.configure(serdeProps, false);

        final Deserializer<TruckPosition> truckPositionDeserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", TruckPosition.class);
        truckPositionDeserializer.configure(serdeProps, false);
        
        final Serde<TruckPosition> truckPositionSerde = Serdes.serdeFrom(truckPositionSerializer, truckPositionDeserializer);

        final Serializer<TruckPositionDriver> truckPositionDriverSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", Driver.class);
        truckPositionDriverSerializer.configure(serdeProps, false);

        final Deserializer<TruckPositionDriver> truckPositionDriverDeserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", Driver.class);
        truckPositionDriverDeserializer.configure(serdeProps, false);
                
		/*
		 * Consume TruckPositions data from Kafka topic
		 */
		KStream<String, TruckPosition> positions = builder.stream("truck_position", Consumed.with(Serdes.String(), truckPositionSerde));
//		positions.print(Printed.toSysOut());

		/*
		 * Non stateful transformation => filter out normal behaviour
		 */
		KStream<String, TruckPosition> filtered = positions.filter(TruckPosition::filterNonNORMAL);
		
		// just for debugging
		// same as: xxx.foreach((key, value) -> System.out.println(key + ", " + value))
		filtered.print(Printed.toSysOut());

		/*
		 * Repartition to prepare for the join
		 */
		KStream<String, TruckPosition> filteredRekeyed = filtered
				.selectKey((key,value) -> value.driverId.toString());
		
		
		// used to be new KafkaStreams(build, streamsConfiguration)
		final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
		
		// clean up all local state by application-id
		streams.cleanUp();

	    streams.setUncaughtExceptionHandler((Thread thread, Throwable throwable) -> {
	    	System.out.println("Within UncaughtExceptionHandler =======>");
	    	System.out.println(throwable);
	    	  // here you should examine the throwable/exception and perform an appropriate action!
	    	});

		streams.start();

	    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
	    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	    
	}

}
