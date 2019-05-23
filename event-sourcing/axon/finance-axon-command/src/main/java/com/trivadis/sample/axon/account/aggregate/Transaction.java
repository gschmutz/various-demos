package com.trivadis.sample.axon.account.aggregate;

import java.math.BigDecimal;
import java.util.UUID;

public abstract class Transaction {
	private UUID id;
	private BigDecimal amount;
	private long when;
	
	public Transaction(UUID id, BigDecimal amount, long when) {
		super();
		this.id = id;
		this.amount = amount;
		this.when = when;
	}

	public UUID getId() {
		return id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public long getWhen() {
		return when;
	}

	@Override
	public String toString() {
		return "Transaction [id=" + id + ", amount=" + amount + ", when=" + when + "]";
	}
	
}
