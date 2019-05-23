package com.trivadis.sample.axon.account.command;

import java.math.BigDecimal;

/**
 * @author saikatkar1
 *
 */
public class DepositMoneyCommand extends BaseCommand<String> {
	private BigDecimal amount;

	public DepositMoneyCommand(String id, BigDecimal amount) {
		super(id);
		this.amount = amount;
	}
	public DepositMoneyCommand() {
		
	}
	public BigDecimal getAmount() {
		return amount;
	}
	
	@Override
	public String toString() {
		return "DepositMoneyCommand [amount=" + amount + ", getId()=" + getId() + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}
	
	
}