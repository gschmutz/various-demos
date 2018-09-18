package com.trivadis.kafkastreams;

public class Driver {
	public Long id;
	public String first_name;
	public String last_name;
	public String available;
	public Long birthdate;
	public Long last_update;
	
	@Override
	public String toString() {
		return "Driver [id=" + id + ", first_name=" + first_name + ", last_name=" + last_name + ", available="
				+ available + ", birthdate=" + birthdate + ", last_update=" + last_update + "]";
	}
	
}


// {"id":27,"first_name":"Walter","last_name":"  Leonard","available":"Y","birthdate":6829,"last_update":1521558837754}