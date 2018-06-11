package com.trivadis.kafkastreams;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.KeyValue;

public class TruckPosition {
	public Long timestamp;
	public Integer truckId;
	public Integer driverId;
	public Integer routeId;
	public String eventType;
	public Double latitude;
	public Double longitude;
	public String correlationId;
	
	public TruckPosition() {		
	}
	
	public static TruckPosition create(String csvRecord) {
		TruckPosition truckPosition = new TruckPosition();
		String[] values = StringUtils.split(csvRecord, ',');
		truckPosition.timestamp = new Long(values[0]);
		truckPosition.truckId = new Integer(values[1]);
		truckPosition.driverId = new Integer(values[2]);		
		truckPosition.routeId = new Integer(values[3]);	
		truckPosition.eventType = values[4];
		truckPosition.latitude = new Double(values[5]);
		truckPosition.longitude = new Double(values[6]);
		truckPosition.correlationId = values[7];
		
		return truckPosition;
	}
	
	public static KeyValue<String, TruckPosition> create(String key, String csvRecord) {
		TruckPosition truckPosition = new TruckPosition();
		String[] values = StringUtils.split(csvRecord, ',');
		truckPosition.timestamp = new Long(values[0]);
		truckPosition.truckId = new Integer(values[1]);
		truckPosition.driverId = new Integer(values[2]);		
		truckPosition.routeId = new Integer(values[3]);	
		truckPosition.eventType = values[4];
		truckPosition.latitude = new Double(values[5]);
		truckPosition.longitude = new Double(values[6]);
		truckPosition.correlationId = values[7];
		
		key = truckPosition.truckId.toString();
		
		return new KeyValue<String, TruckPosition>(key, truckPosition);
	}
	

    public static boolean filterNonNORMAL(String key, TruckPosition value) {
        boolean result = false;
        result = !value.eventType.equals("Normal");
        return result;
    }	
	
	@Override
	public String toString() {
		return "TruckPosition [timestamp=" + timestamp + ", truckId=" + truckId + ", driverId=" + driverId + ", routeId=" + routeId
				+ ", eventType=" + eventType + ", latitude=" + latitude + ", longitude="
				+ longitude + ", correlationId=" + correlationId + "]";
	}
	
	public String toCSV() {
		return timestamp + "," + truckId + "," + driverId + "," + routeId + "," + eventType + "," + latitude + "," + longitude + "," + correlationId;
	}
	
}
