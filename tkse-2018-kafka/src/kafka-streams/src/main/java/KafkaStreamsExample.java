import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.Predicate;

public class KafkaStreamsExample {

	public static void main(String[] args) {
		// Serializers/deserializers (serde) for String and Long types
		final Serde<String> stringSerde = Serdes.String();
		final Serde<Long> longSerde = Serdes.Long();

	    final String bootstrapServers = args.length > 0 ? args[0] : "192.168.69.136:9092";
	    final Properties streamsConfiguration = new Properties();
	    // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
	    // against which the application is run.
	    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-example");
	    streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "kafka-streams-example-client");
	    // Where to find Kafka broker(s).
	    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
	    // Specify default (de)serializers for record keys and for record values.
	    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
	    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());		
		
	    
		// In the subsequent lines we define the processing topology of the Streams application.
		final KStreamBuilder builder = new KStreamBuilder();
		// Construct a `KStream` from the input topic "streams-plaintext-input", where message values
		// represent lines of text (for the sake of this example, we ignore whatever may be stored
		// in the message keys).
		KStream<String, String> source = builder.stream(stringSerde, stringSerde, "all-stream");
		
		KStream<String, SensorValue> positions = source.map((key,value) -> new KeyValue<>(key, SensorValue.create(value)));
		
		KStream<String, SensorValue> filtered = positions.filter(SensorValue::filterNonNORMAL);
		
		filtered.foreach(new ForeachAction<String, SensorValue>() {
		    public void apply(String key, SensorValue value) {
		        System.out.println(key + ": " + value);
		    }
		 });
		
		filtered.map((key,value) -> new KeyValue<>(key,value._originalRecord)).to("all-filtered");

		final KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
		streams.cleanUp();
	    streams.start();

	    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
	    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}

}
