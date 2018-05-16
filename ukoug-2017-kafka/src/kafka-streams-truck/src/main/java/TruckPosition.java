import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.KeyValue;

public class TruckPosition {
	String ts;
	String truckId;
	String driverId;
	BigInteger routeId;
	String routeName;
	String eventType;
	Double latitude;
	Double longitude;
	String correlationId;
	
	String _originalRecord;
	
	public TruckPosition() {		
	}
	
	public static TruckPosition create(String csvRecord) {
		TruckPosition truckPosition = new TruckPosition();
		String[] values = StringUtils.split(csvRecord, ',');
		truckPosition.ts = values[0];
		truckPosition.truckId = values[1];
		truckPosition.driverId = values[2];		
		truckPosition.routeId = new BigInteger(values[3]);	
		truckPosition.routeName = values[4];
		truckPosition.eventType = values[5];
		truckPosition.latitude = new Double(values[6]);
		truckPosition.longitude = new Double(values[7]);
		truckPosition.correlationId = values[8];
		
		truckPosition._originalRecord = csvRecord;
		
		return truckPosition;
	}
	
	public static KeyValue<String, TruckPosition> parse(String key, String csvRecord) {
		TruckPosition truckPosition = new TruckPosition();
		String[] values = StringUtils.split(csvRecord, ',');
		truckPosition.ts = values[0];
		truckPosition.truckId = values[1];
		truckPosition.driverId = values[2];		
		truckPosition.routeId = new BigInteger(values[3]);	
		truckPosition.routeName = values[4];
		truckPosition.eventType = values[5];
		truckPosition.latitude = new Double(values[6]);
		truckPosition.longitude = new Double(values[7]);
		truckPosition.correlationId = values[8];
		
		return new KeyValue<String, TruckPosition>(key, truckPosition);
	}
	

    public static boolean filterNonNORMAL(String key, TruckPosition value) {
        boolean result = false;
        result = !value.eventType.equals("Normal");
        return result;
}	
	
	@Override
	public String toString() {
		return "TruckPosition [ts=" + ts + ", truckId=" + truckId + ", driverId=" + driverId + ", routeId=" + routeId
				+ ", routeName=" + routeName + ", eventType=" + eventType + ", latitude=" + latitude + ", longitude="
				+ longitude + ", correlationId=" + correlationId + "]";
	}
	
}
