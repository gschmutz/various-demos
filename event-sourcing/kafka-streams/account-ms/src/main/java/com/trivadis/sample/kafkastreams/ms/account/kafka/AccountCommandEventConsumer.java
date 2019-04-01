package com.trivadis.sample.kafkastreams.ms.account.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.trivadis.avro.command.account.v1.AccountCreateCommand;

@Component
public class AccountCommandEventConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountCommandEventConsumer.class);

	@KafkaListener(topics = "${kafka.topic.account.command}")
	public void receive(ConsumerRecord<String, String> accountCreateCommand) {
		String accountCreateCommandValue = accountCreateCommand.value();
		LOGGER.info("received payload='{}'", accountCreateCommandValue.toString());
		

	}
}
