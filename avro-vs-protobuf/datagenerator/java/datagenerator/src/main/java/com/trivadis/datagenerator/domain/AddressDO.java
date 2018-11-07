package com.trivadis.datagenerator.domain;

public class AddressDO {
	private Integer id;
	private String steetAndNr;
	private String zipAndCity;
	
	public void setId(Integer id) {
		this.id = id;
	}
	public void setSteetAndNr(String steetAndNr) {
		this.steetAndNr = steetAndNr;
	}
	public void setZipAndCity(String zipAndCity) {
		this.zipAndCity = zipAndCity;
	}
	public AddressDO(Integer id, String steetAndNr, String zipAndCity) {
		super();
		this.id = id;
		this.steetAndNr = steetAndNr;
		this.zipAndCity = zipAndCity;
	}
	public Integer getId() {
		return id;
	}
	public String getSteetAndNr() {
		return steetAndNr;
	}
	public String getZipAndCity() {
		return zipAndCity;
	}
	
	
}
