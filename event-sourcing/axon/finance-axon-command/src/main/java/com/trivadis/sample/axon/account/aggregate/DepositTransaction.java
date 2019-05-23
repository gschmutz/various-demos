package com.trivadis.sample.axon.account.aggregate;

import java.math.BigDecimal;
import java.util.UUID;

public class DepositTransaction extends Transaction {

	public DepositTransaction(UUID id, BigDecimal amount, long when) {
		super(id, amount, when);
		
	}

}
