package com.hortonworks.simulator.impl.domain.carinsurance;

public enum DrivingEventTypeEnum {
	LANE_DEPARTURE("Lane Departure"), TOO_CLOSE_FOLLOW(
			"Unsafe following distance"), OVERSPEED("Overspeed"), HARD_BRAKING(
			"Hard Braking"), SWERVING("Swerving"), NORMAL("Normal");

	private final String eventType;

	DrivingEventTypeEnum(String eventType) {
		this.eventType = eventType;
	}

	@Override
	public String toString() {
		return eventType;
	}
}
