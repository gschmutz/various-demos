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

import de.tkse.sample.SensorGroupOne;

public class AvroSensorGroupProducer {
	private Producer<String, SensorGroupOne> producer = null;
	private String kafkaTopicSensorGroupOne = "sensor-group-1-v1";

	private Producer<String, SensorGroupOne> connect() {
		Producer<String, SensorGroupOne> producer = null;

		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("key.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
		props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
		props.put("schema.registry.url", "http://localhost:8081");

		try {
			producer = new KafkaProducer<String, SensorGroupOne>(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return producer;
	}

	public void produce(SensorGroupOne value) throws IOException {
		final Random rnd = new Random();

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		if (producer == null) {
			producer = connect();
		}

		Integer key = rnd.nextInt(255);

		ProducerRecord<String, SensorGroupOne> record = new ProducerRecord<String, SensorGroupOne>(
				kafkaTopicSensorGroupOne, null, value);

		if (producer != null) {
			try {
				Future<RecordMetadata> future = producer.send(record);
				RecordMetadata metadata = future.get();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}

		}

	}

	public static void main(String[] args) {
		AvroSensorGroupProducer producer = new AvroSensorGroupProducer();

		SensorGroupOne value = new SensorGroupOne(0.1d, 0.2d);
		try {
			producer.produce(value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
