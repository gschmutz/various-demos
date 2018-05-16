package kafka.sample.producer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;

import ch.trivadis.sample.twitter.avro.v1.TwitterStatusUpdate;

public class AvroSensorGroupProducer {
	private Producer<String, TwitterStatusUpdate> producer = null;
	private String kafkaTopicTweetStatus = "tweet";

	private Producer<String, TwitterStatusUpdate> connect() {
		Producer<String, TwitterStatusUpdate> producer = null;
    	
		Properties props = new Properties();
	    props.put("bootstrap.servers", "localhost:9092");
	    props.put("acks", "all");
	    props.put("retries", 0);
	    props.put("key.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
	    props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
	    props.put("schema.registry.url", "http://localhost:8081");

		try {
    		producer = new KafkaProducer<String, TwitterStatusUpdate>(props);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return producer;
	}
	
	public void produce(TwitterStatusUpdate status) {
        final Random rnd = new Random();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DatumWriter<TwitterStatusUpdate> writer = new SpecificDatumWriter<TwitterStatusUpdate>(TwitterStatusUpdate.class);
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        try {
			writer.write(status, encoder);
	        encoder.flush();
	        out.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
        
        if (producer == null) {
        	producer = connect();
        }
        
        Integer key = rnd.nextInt(255);
        
        ProducerRecord<String, TwitterStatusUpdate> record = new ProducerRecord<String, TwitterStatusUpdate>(kafkaTopicTweetStatus, null, status);

        if (producer !=null) {
        	try {
        		Future<RecordMetadata> future = producer.send(record);
        		RecordMetadata metadata = future.get();
        	} catch (Exception e) {
        		System.err.println(e.getMessage());
        		e.printStackTrace();
        	}
        	
        }
		
	}

}
