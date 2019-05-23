package com.trivadis.sample.axon.account.model;

public class Transaction {
	private Double amount;
	private long when;
	
	public Transaction(Double amount, long when) {
		super();
		this.amount = amount;
		this.when = when;
	}

	public Double getAmount() {
		return amount;
	}

	public long getWhen() {
		return when;
	}
	
	
}
