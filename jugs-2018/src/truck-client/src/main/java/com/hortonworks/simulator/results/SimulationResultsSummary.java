package com.hortonworks.simulator.results;

public class SimulationResultsSummary {
	private int eventEmitters = 0;

	public SimulationResultsSummary(int numberOfMessages) {
		this.eventEmitters = numberOfMessages;
	}

	public String toString() {
		return "System generated " + eventEmitters + " EventEmitters";
	}
}
