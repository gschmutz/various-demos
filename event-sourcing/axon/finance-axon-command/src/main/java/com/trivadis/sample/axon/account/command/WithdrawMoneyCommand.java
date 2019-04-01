package com.trivadis.sample.axon.account.command;

import java.math.BigDecimal;

public class WithdrawMoneyCommand extends BaseCommand<String> {
	private  BigDecimal amount;

	public WithdrawMoneyCommand(String id, BigDecimal amount) {
		super(id);
		this.amount = amount;
	}
	public WithdrawMoneyCommand() {}
	
	public BigDecimal getAmount() {
		return amount;
	}
}