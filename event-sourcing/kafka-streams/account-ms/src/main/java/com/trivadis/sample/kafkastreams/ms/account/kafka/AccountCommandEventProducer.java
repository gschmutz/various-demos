package com.trivadis.sample.kafkastreams.ms.account.kafka;

import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trivadis.sample.kafkastreams.ms.account.command.AccountCreateCommand;
import com.trivadis.sample.kafkastreams.ms.account.command.DepositMoneyCommand;
import com.trivadis.sample.kafkastreams.ms.account.command.WithdrawMoneyCommand;


@Component
public class AccountCommandEventProducer {
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Value("${kafka.topic.account.command}")
	String kafkaTopicAccountCommand;
	
	@Value("${kafka.topic.account-created}")
	String kafkaTopicAccountCreated;

	public void produce(AccountCreateCommand accountCreateCommand) {
		final ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			ProducerRecord<String, String> record = new ProducerRecord<> (kafkaTopicAccountCommand,
																accountCreateCommand.getId().toString(), 
																objectMapper.writeValueAsString(accountCreateCommand));
			record.headers().add(new RecordHeader("command", "AccountCreateCommand".getBytes()));
			kafkaTemplate.send(record);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void produce(DepositMoneyCommand depositMoneyCommand) {
		final ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			ProducerRecord<String, String> record = new ProducerRecord<> (kafkaTopicAccountCommand,
																		depositMoneyCommand.getId().toString(), 
																		objectMapper.writeValueAsString(depositMoneyCommand));
			record.headers().add(new RecordHeader("command", "DepositMoneyCommand".getBytes()));
			kafkaTemplate.send(record);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void produce(WithdrawMoneyCommand withdrawMoneyCommand) {
		final ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			ProducerRecord<String, String> record = new ProducerRecord<> (kafkaTopicAccountCommand,
																		withdrawMoneyCommand.getId().toString(), 
																		objectMapper.writeValueAsString(withdrawMoneyCommand));
			record.headers().add(new RecordHeader("command", "WithdrawMoneyCommand".getBytes()));
			kafkaTemplate.send(record);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
