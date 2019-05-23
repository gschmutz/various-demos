package com.trivadis.sample.axon.account.aggregate;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.trivadis.sample.axon.account.command.AccountCreateCommand;
import com.trivadis.sample.axon.account.command.DepositMoneyCommand;
import com.trivadis.sample.axon.account.command.WithdrawMoneyCommand;
import com.trivadis.sample.axon.account.event.AccountCreatedEvent;
import com.trivadis.sample.axon.account.event.MoneyDepositedEvent;
import com.trivadis.sample.axon.account.event.MoneyWithdrawnEvent;
import com.trivadis.sample.axon.account.exception.InsufficientBalanceException;

@Aggregate
public class AccountAggregate{
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountAggregate.class);

	@Autowired
	private EventSourcingRepository<AccountAggregate> repo;
	
	@AggregateIdentifier
	private String id;
	
	private BigDecimal balance;
	private String forCustomerId;
	private String accountType;

	private List<Transaction> transactions;
	
	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getForCustomerId() {
		return forCustomerId;
	}

	public void setForCustomerId(String forCustomerId) {
		this.forCustomerId = forCustomerId;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public AccountAggregate() {
		// constructor needed for reconstructing the aggregate
		transactions = new ArrayList<Transaction>();
		LOGGER.info("(F) Empty Account Aggregate created");
	}
	
	@CommandHandler
	public AccountAggregate(AccountCreateCommand command) {
		LOGGER.info("(C) Handle: " + command.toString());
		
		Assert.hasLength(command.getForCustomerId(), "CustomerId must have a value");
		Assert.hasLength(command.getAccountType(), "AccountType must have a value");
		Assert.hasLength(command.getId(), "Account id must have length greater than Zero");
		apply(new AccountCreatedEvent(command.getId(), command.getForCustomerId(), command.getAccountType(), new BigDecimal("0")));
	}
	
	@EventSourcingHandler
	public void handle(AccountCreatedEvent event) {
		LOGGER.info("(E) Handle "+ event.toString());
		
		this.id = event.getId();
		this.forCustomerId = event.getForCustomerId();
		this.accountType = event.getAccountType();
		this.balance = event.getBalance();
	}
	
	@CommandHandler
	public void on(DepositMoneyCommand command) {
		LOGGER.info("(C) Handle: " + command.toString());
		Assert.isTrue(command.getAmount().compareTo(BigDecimal.ZERO) > 0 , "Amount should be a positive number");
		apply(new MoneyDepositedEvent(command.getId(), command.getAmount(), new Date().getTime()));
	}
	
	@EventSourcingHandler
	public void handle(MoneyDepositedEvent event) {
		LOGGER.info("(E) Handle "+ event.toString());
		//AccountAggregate aggregate = repository.load(event.getId()).getWrappedAggregate().getAggregateRoot();
		this.balance = this.balance.add(event.getAmount());

		// add the withdrawn transaction
		transactions.add(new DepositTransaction(UUID.randomUUID(), event.getAmount(), new Date().getTime()));
		
	}
	
	@CommandHandler
	public void on(WithdrawMoneyCommand command) {
		LOGGER.info("(C) Handle: " + command.toString());
		Assert.isTrue(command.getAmount().compareTo(BigDecimal.ZERO) > 0 , "Amount should be a positive number");
		if(command.getAmount().compareTo(this.balance) > 0 ) {
			throw new InsufficientBalanceException("Insufficient balance. Trying to withdraw:" + command.getAmount() + ", but current balance is: " + this.balance);
		}
		apply(new MoneyWithdrawnEvent(command.getId(), command.getAmount(), new Date().getTime()));
	}
	
	@EventSourcingHandler
	public void handle(MoneyWithdrawnEvent event) {
		LOGGER.info("(E) Handle "+ event.toString());
		this.balance = this.balance.subtract((event.getAmount()));
		
		// add the withdrawn transaction
		transactions.add(new WithdrawTransaction(UUID.randomUUID(), event.getAmount(), new Date().getTime()));
	}

}
