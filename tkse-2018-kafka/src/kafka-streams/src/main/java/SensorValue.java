import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.KeyValue;

public class SensorValue {
	String ts;
	String value;
	
	String _originalRecord;
	
	public SensorValue() {		
	}
	
	public static SensorValue create(String csvRecord) {
		SensorValue truckPosition = new SensorValue();
		String[] values = StringUtils.split(csvRecord, ',');
		truckPosition.ts = values[0];
		truckPosition.value = values[1];
		
		truckPosition._originalRecord = csvRecord;
		
		return truckPosition;
	}
	
	public static KeyValue<String, SensorValue> parse(String key, String csvRecord) {
		SensorValue truckPosition = new SensorValue();
		String[] values = StringUtils.split(csvRecord, ',');
		truckPosition.ts = values[0];
		truckPosition.value = values[1];
		
		return new KeyValue<String, SensorValue>(key, truckPosition);
	}
	

    public static boolean filterNonNORMAL(String key, SensorValue value) {
        boolean result = false;
//        result = !value.eventType.equals("Normal");
        return result;
}

	@Override
	public String toString() {
		return "SensorValue [ts=" + ts + ", value=" + value + ", _originalRecord=" + _originalRecord + "]";
	}	
	
	
}
