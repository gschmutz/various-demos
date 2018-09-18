package com.trivadis.kafkastreams.json;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import com.fasterxml.jackson.databind.JsonNode;
import com.trivadis.kafkastreams.TruckPosition;

/**
 * A timestamp extractor implementation that tries to extract event time from
 * the "timestamp" field in the Json formatted message.
 */
public class JsonTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(final ConsumerRecord<Object, Object> record, final long previousTimestamp) {
        if (record.value() instanceof TruckPosition) {
            return ((TruckPosition) record.value()).timestamp;
        }

        if (record.value() instanceof JsonNode) {
            return ((JsonNode) record.value()).get("timestamp").longValue();
        }

        throw new IllegalArgumentException("JsonTimestampExtractor cannot recognize the record value " + record.value());
    }
}