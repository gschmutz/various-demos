package com.hortonworks.simulator.impl.domain.transport;

public enum MobileEyeEventTypeEnum {
	LANE_DEPARTURE("Lane Departure"), TOO_CLOSE_FOLLOW(
			"Unsafe following distance"), TOO_CLOSE_TAIL("Unsafe tail distance"), OVERSPEED(
			"Overspeed"), NORMAL("Normal");

	private final String eventType;

	MobileEyeEventTypeEnum(String eventType) {
		this.eventType = eventType;
	}

	@Override
	public String toString() {
		return eventType;
	}

	public String toCSV() {
		return eventType;
	}
}
