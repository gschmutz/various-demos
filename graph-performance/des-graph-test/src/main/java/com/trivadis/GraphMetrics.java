
package com.trivadis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

public class GraphMetrics {
	private DateTime timestamp = DateTime.now();
	private Map<String, Integer> values = new HashMap<String, Integer>();

	public final String ALL = "ALL";
	
	public GraphMetrics(Map<String, Integer> values, DateTime timestamp) {
		this.values = values;
		this.timestamp = timestamp;
	}
	
	public GraphMetrics() {
		
	}

	public Map<String, Integer> getValues() {
		return values;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}
	
	public void add(Map<String,Integer> metrics) {
		values.putAll(metrics);
		for (String metricFullName : metrics.keySet()) {
			values.put(getMetricType(metricFullName) + "." + getMetricMethod(metricFullName) + "." + ALL, getMetricValue(metricFullName));
		}
	}
	
	public Integer getMetricValue(String metricFullName) {
		return values.get(metricFullName);
	}
	
	public String getMetricType(String metricFullName) {
		return StringUtils.substringBefore(metricFullName, ".");
	}

	public String getMetricMethod(String metricFullName) {
		return StringUtils.substringBefore(StringUtils.substringAfter(metricFullName, "."),".");
	}
	
	public String getMetricObject(String metricFullName) {
		return StringUtils.substringAfter(StringUtils.substringAfter(metricFullName, "."),".");
	}
	
	public Set<String> getMetricFullNames() {
		return values.keySet();
	}
	
	@Override
	public String toString() {
		return "GraphMetrics [timestamp=" + timestamp + ", values=" + values + "]";
	}

}
