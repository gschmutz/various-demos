package com.trivadis.sample.kafkastreams.ms.account.aggregate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trivadis.sample.kafkastreams.ms.account.command.AccountCreateCommand;
import com.trivadis.sample.kafkastreams.ms.account.command.DepositMoneyCommand;
import com.trivadis.sample.kafkastreams.ms.account.command.WithdrawMoneyCommand;
import com.trivadis.sample.kafkastreams.ms.account.event.AccountCreatedEvent;
import com.trivadis.sample.kafkastreams.ms.account.event.MoneyDepositedEvent;
import com.trivadis.sample.kafkastreams.ms.account.event.MoneyWithdrawnEvent;
import com.trivadis.sample.kafkastreams.ms.account.kafka.AccountCommandEventProducer;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class AccountAggregate {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountAggregate.class);

	@Value("${kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${kafka.schema-registry-url}")
	private String schemaRegistryUrl;

	@Value("${kafka-streams.applicationId}")
	private String applicationId;

	@Value("${kafka.topic.account.command}")
	String kafkaTopicAccountCommand;
	
	@Value("${kafka.topic.account.snapshot}")
	String kafkaTopicAccountSnapshot;
	
	@Value("${kafka.topic.account-created}")
	String kafkaTopicAccountCreated;
	
	@Value("${kafka.topic.money-deposited}")
	String kafkaTopicMoneyDeposited;
	
	@Value("${kafka.topic.money-withdrawn}")
	String kafkaTopicMoneyWithdrawn;
	
	@Autowired
	private AccountCommandEventProducer accountCommandProducer;

	@Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
	public KafkaStreamsConfiguration kafkaStreamsConfigs() {
		System.out.println("=====> in kafkaStreamsConfigs");
		Map<String, Object> props = new HashMap<>();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		// Where to find the Confluent schema registry instance(s)
		props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

		// Specify default (de)serializers for record keys and for record values.
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, StringSerde.class);
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, StringSerde.class);

		// props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG,
		// WallclockTimestampExtractor.class.getName());
		return new KafkaStreamsConfiguration(props);
	}

	@Bean
	public KStream<?, ?> kafkaStream(StreamsBuilder kStreamBuilder) {

	    // json Serde
	    final Serializer<JsonNode> jsonSerializer = new JsonSerializer();
	    final Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
	    final Serde<JsonNode> jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);	
	    final Serde<Account> accountSerde = Serdes.serdeFrom(new AccountSerializer(), new AccountDeserializer());

		final StoreBuilder<KeyValueStore<String, Account>> accountSnapshotStore = Stores
				.keyValueStoreBuilder(Stores.persistentKeyValueStore("AccountSnapshotStore"), Serdes.String(), accountSerde)
				.withCachingEnabled();
		kStreamBuilder.addStateStore(accountSnapshotStore);

		KStream<String, JsonNode> stream = kStreamBuilder.stream(kafkaTopicAccountCommand, Consumed.with(Serdes.String(), jsonSerde));

		//  check if it should be checked for expiration of a record against the store
		KStream<String, JsonNode> eventStream = stream.transformValues(() -> new CommandHandler(accountSnapshotStore.name()), accountSnapshotStore.name());
		eventStream.transformValues(() -> new EventHandler(accountSnapshotStore.name()), accountSnapshotStore.name()).to(kafkaTopicAccountSnapshot, Produced.with(Serdes.String(), jsonSerde));
		
		KStream<String, JsonNode>[] eventStreams = eventStream.branch(
					(key,value) -> (value.get("__eventType").asText().equals("AccountCreatedEvent")),
					(key,value) -> (value.get("__eventType").asText().equals("MoneyDepositedEvent")),
					(key,value) -> true
					);
		// AccountCreatedEvent
		eventStreams[0].to(kafkaTopicAccountCreated, Produced.with(Serdes.String(), jsonSerde));

		// MoneyDepositedEvent
		eventStreams[1].to(kafkaTopicMoneyDeposited, Produced.with(Serdes.String(), jsonSerde));

		// MoneyWithdrawnEvent
		eventStreams[2].to(kafkaTopicMoneyWithdrawn, Produced.with(Serdes.String(), jsonSerde));
		

		LOGGER.info("Stream started here...");
		return stream;
	}
	

	private static final class CommandHandler implements ValueTransformer<JsonNode, JsonNode> {

		final private String storeName;
	    private KeyValueStore<String, Account> stateStore;
	    private ProcessorContext context;
	    
		public CommandHandler(final String storeName) {
	        Objects.requireNonNull(storeName,"Store Name can't be null");
			this.storeName = storeName;
		}

	    @Override
	    @SuppressWarnings("unchecked")
	    public void init(ProcessorContext context) {
	        this.context = context;
	        stateStore = (KeyValueStore) this.context.getStateStore(storeName);
	    }

		@Override
		public JsonNode transform(JsonNode value) {
			JsonNode json = null;
			Account accountSnapshot= null;
			String command = value.get("__command").asText();
			final ObjectMapper objectMapper = new ObjectMapper();

			try {
				switch (command) {
				case "AccountCreateCommand":
					AccountCreateCommand acc = objectMapper.treeToValue(value, AccountCreateCommand.class);
					
					if (stateStore.get(acc.getId()) != null) {
						throw new RuntimeException("an account witht the id " + acc.getId() + " already exists!");
					} else {
						AccountCreatedEvent ace = new AccountCreatedEvent(acc.getId(), acc.getForCustomerId(), acc.getAccountType(), new BigDecimal(0));
						json = objectMapper.valueToTree(ace);
					}

					break;
				case "DepositMoneyCommand":
					DepositMoneyCommand dmc = objectMapper.treeToValue(value, DepositMoneyCommand.class);
					
					accountSnapshot = stateStore.get(dmc.getId());
					if (accountSnapshot != null) {
						MoneyDepositedEvent mde = new MoneyDepositedEvent(dmc.getId(), dmc.getAmount());
						json = objectMapper.valueToTree(mde);
					}
					break;
				case "WithdrawMoneyCommand":
					WithdrawMoneyCommand wmc = objectMapper.treeToValue(value, WithdrawMoneyCommand.class);
					
					accountSnapshot = stateStore.get(wmc.getId());
					if (accountSnapshot != null) {
						if (accountSnapshot.getBalance().compareTo(wmc.getAmount()) > 0) {
							MoneyWithdrawnEvent mwe = new MoneyWithdrawnEvent(wmc.getId(), wmc.getAmount());
							json = objectMapper.valueToTree(mwe);
						}
					}
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Json in transform" + json);
			return json;
		}


		@Override
		public void close() {
			// TODO Auto-generated method stub
			
		}


	}
	
	private static final class EventHandler implements ValueTransformer<JsonNode, JsonNode> {

		final private String storeName;
	    private KeyValueStore<String, Account> stateStore;
	    private ProcessorContext context;
	    
		public EventHandler(final String storeName) {
	        Objects.requireNonNull(storeName,"Store Name can't be null");
			this.storeName = storeName;
		}

	    @Override
	    @SuppressWarnings("unchecked")
	    public void init(ProcessorContext context) {
	        this.context = context;
	        stateStore = (KeyValueStore) this.context.getStateStore(storeName);
	    }

		@Override
		public JsonNode transform(JsonNode value) {
			JsonNode json = null;
			Account accountSnapshot = null;
			String eventType = value.get("__eventType").asText();
			final ObjectMapper objectMapper = new ObjectMapper();

			try {
				switch (eventType) {
					case "AccountCreatedEvent":
						AccountCreatedEvent ace = objectMapper.treeToValue(value, AccountCreatedEvent.class);
						accountSnapshot = new Account(ace.getId(), ace.getForCustomerId(), ace.getForCustomerId(), new BigDecimal(0));
						
						stateStore.put(ace.getId(), accountSnapshot);
						
						break;
					case "MoneyDepositedEvent":
						MoneyDepositedEvent mde = objectMapper.treeToValue(value, MoneyDepositedEvent.class);
						accountSnapshot = stateStore.get(mde.getId());

						accountSnapshot.setBalance(accountSnapshot.getBalance().add(mde.getAmount()));
						
						stateStore.put(mde.getId(), accountSnapshot);
						break;
					case "MoneyWithdrawnEvent":
						MoneyDepositedEvent mwe = objectMapper.treeToValue(value, MoneyDepositedEvent.class);
						accountSnapshot = stateStore.get(mwe.getId());
						
						accountSnapshot.setBalance(accountSnapshot.getBalance().subtract(mwe.getAmount()));
						
						stateStore.put(mwe.getId(), accountSnapshot);
						break;				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (accountSnapshot != null) {
				json = objectMapper.valueToTree(accountSnapshot);
			}
			return json;
		}


		@Override
		public void close() {
			// TODO Auto-generated method stub
			
		}


	}	
	
	private static final class Account {
		private String id;
		private String forCustomerId;
		private String accountType;
		private BigDecimal balance;
		
		public Account(String id, String forCustomerId, String accountType, BigDecimal balance) {
			super();
			this.id = id;
			this.forCustomerId = forCustomerId;
			this.accountType = accountType;
			this.balance = balance;
		}

		public Account() { }
		
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getForCustomerId() {
			return forCustomerId;
		}

		public void setForCustomerId(String forCustomerId) {
			this.forCustomerId = forCustomerId;
		}

		public String getAccountType() {
			return accountType;
		}

		public void setAccountType(String accountType) {
			this.accountType = accountType;
		}

		public BigDecimal getBalance() {
			return balance;
		}

		public void setBalance(BigDecimal balance) {
			this.balance = balance;
		}

	}
	
	public class AccountSerializer implements Serializer<Account> {

		@Override public void configure(Map<String, ?> map, boolean b) { }

		@Override public byte[] serialize(String topic, Account data) {
		    byte[] retVal = null;

		    ObjectMapper objectMapper = new ObjectMapper();
		    try {
		      retVal = objectMapper.writeValueAsString(data).getBytes();
		    } catch (Exception e) {
		      e.printStackTrace();
		    }

		    return retVal;
		}

		@Override public void close() { }
	}

	public class AccountDeserializer implements Deserializer<Account> {

		@Override public void close() { }

		@Override public void configure(Map<String, ?> arg0, boolean arg1) { }

		@Override
		public Account deserialize(String toipic, byte[] data) {

		    ObjectMapper mapper = new ObjectMapper();

		    Account account = null;

		    try {
		      account = mapper.readValue(data, Account.class);
		    } catch (Exception e) {
		      e.printStackTrace();
		    }
		    return account;
		}
	}	
	
	public void performAccountCreateCommand(AccountCreateCommand command) {

		Assert.hasLength(command.getForCustomerId(), "CustomerId must have a value");
		Assert.hasLength(command.getAccountType(), "AccountType must have a value");
		Assert.hasLength(command.getId(), "Account id must have length greater than Zero");

		accountCommandProducer.produce(command);
	}

	public void performDepositMoneyCommand(DepositMoneyCommand command) {

		Assert.hasLength(command.getId(), "Account id must have length greater than Zero");

		accountCommandProducer.produce(command);
	}

	public void performWithdrawMoneyCommand(WithdrawMoneyCommand command) {

		Assert.hasLength(command.getId(), "Account id must have length greater than Zero");

		accountCommandProducer.produce(command);
	}
}
