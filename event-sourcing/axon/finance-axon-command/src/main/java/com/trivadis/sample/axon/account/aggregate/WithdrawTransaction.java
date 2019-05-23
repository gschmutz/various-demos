package com.trivadis.sample.axon.account.aggregate;

import java.math.BigDecimal;
import java.util.UUID;

public class WithdrawTransaction extends Transaction {

	public WithdrawTransaction(UUID id, BigDecimal amount, long when) {
		super(id, amount, when);
		// TODO Auto-generated constructor stub
	}

}
