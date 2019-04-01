package com.trivadis.sample.kafkastreams.ms.account;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.trivadis.avro.command.account.v1.AccountCreateCommand;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;

@Configuration
public class KafkaConfig {
	  @Value("${kafka.bootstrap-servers}")
	  private String bootstrapServers;

	  @Value("${kafka.schema-registry-url}")
	  private String schemaRegistryURL;	  

	  // Producer Configuration
	  
	  @Bean
	  public Map<String, Object> producerConfigs() {
	    Map<String, Object> props = new HashMap<>();
	    System.out.println(bootstrapServers);
	    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
	    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//	    props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);
//	    props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);
//	    props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, io.confluent.kafka.serializers.subject.RecordNameStrategy.class);
//	    props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, io.confluent.kafka.serializers.subject.TopicRecordNameStrategy.class);

	    return props;
	  }
	  
	  @Bean
	  public ProducerFactory<String, String> producerFactory() {
	    return new DefaultKafkaProducerFactory<>(producerConfigs());
	  }

	  @Bean
	  public KafkaTemplate<String, String> kafkaTemplate() {
	    return new KafkaTemplate<>(producerFactory());
	  }

	  // Consumer Configuration
	  
	  @Bean
	  public Map<String, Object> consumerConfigs() {
	    Map<String, Object> props = new HashMap<>();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, "accountAggregated");
//	    props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);
//	    props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);
	    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

	    return props;
	  }
	  
	  @Bean
	  public ConsumerFactory<String, String> consumerFactory() {
	    return new DefaultKafkaConsumerFactory<>(consumerConfigs());
	  }
	  
	  @Bean
	  public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
	    ConcurrentKafkaListenerContainerFactory<String, String> factory =
	        new ConcurrentKafkaListenerContainerFactory<>();
	    factory.setConsumerFactory(consumerFactory());

	    return factory;
	  }
}
