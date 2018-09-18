package com.trivadis.kafkastreams.csv;
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
import org.apache.kafka.common.utils.Bytes;
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

public class KafkaStreamsExampleCSV {
	
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
		
	    final String bootstrapServers = args.length > 0 ? args[0] : "192.168.25.163:9092";
	    final Properties streamsConfiguration = new Properties();
	    // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
	    // against which the application is run.
	    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-trucking");

	    // Where to find Kafka broker(s).
	    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
	    
	    // Specify default (de)serializers for record keys and for record values.
	    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
	    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());		
	    
	    // specify the TimestampExtrator to use
	    //streamsConfiguration.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, JsonTimestampExtractor.class);
		
		// In the subsequent lines we define the processing topology of the Streams application.
		// used to be KStreamBuilder ....
	    final StreamsBuilder builder = new StreamsBuilder();

		/*
		 * Consume TruckPositions data from Kafka topic
		 */
		KStream<String, String> positions = builder.stream("truck_position", Consumed.with(Serdes.String(), Serdes.String()));

		/*
		 * Map CSV to JSON
		 */
		KStream<String, TruckPosition> positionsCSV = positions.map((key,value) -> TruckPosition.create(key, value.substring(7, value.length())));
		positionsCSV.print(Printed.toSysOut());
		
		/*
		 * Non stateful transformation => filter out normal behaviour
		 */
		KStream<String, TruckPosition> filtered = positionsCSV.filter(TruckPosition::filterNonNORMAL);
		
		filtered.map((key,value) -> new KeyValue<>(key,value.toCSV())).to("dangerous_driving");
		
		// just for debugging
		// same as: xxx.foreach((key, value) -> System.out.println(key + ", " + value))
		//filtered.print(Printed.toSysOut());
		
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
