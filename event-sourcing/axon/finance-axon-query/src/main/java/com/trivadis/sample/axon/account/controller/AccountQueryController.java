package com.trivadis.sample.axon.account.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trivadis.sample.axon.account.event.AccountCreatedEvent;
import com.trivadis.sample.axon.account.event.MoneyDepositedEvent;
import com.trivadis.sample.axon.account.event.MoneyWithdrawnEvent;
import com.trivadis.sample.axon.account.model.Account;
import com.trivadis.sample.axon.account.model.Transaction;

import springbootaxon.account.repo.AccountRepository;


@RestController
@ProcessingGroup("Accounts")
@RequestMapping("/accounts")
public class AccountQueryController {
	
	@Autowired
	private AccountRepository accRepo;
	
	@EventHandler
	public void on(AccountCreatedEvent event,@Timestamp Instant instant) {
		Account account = new Account(event.getId(),event.getBalance(),event.getForCustomerId(),event.getAccountType(),instant.toString());
		
		accRepo.insert(account);
		
	}
	
	@EventHandler
	public void on(MoneyDepositedEvent event,@Timestamp Instant instant) {
		Account account = accRepo.findByAccountNo(event.getId());
		account.setBalance(account.getBalance().add(event.getAmount()));
		account.setLastUpdated(instant.toString());
		
		account.appendTransaction(new Transaction(event.getAmount().doubleValue(), event.getWhen()));

		accRepo.save(account);
	}
	

	@EventHandler
	public void on(MoneyWithdrawnEvent event, @Timestamp Instant instant) {
		Account account = accRepo.findByAccountNo(event.getId());
		account.setBalance(account.getBalance().subtract(event.getAmount()));
		account.setLastUpdated(instant.toString());
		
		account.appendTransaction(new Transaction(event.getAmount().multiply(new BigDecimal(-1)).doubleValue(), event.getWhen()));
		
		accRepo.save(account);
	}
	
	@GetMapping("/details")
	public List<Account> getAccDetails() {
		return accRepo.findAll();
	}
	
	@GetMapping("/details/{id}")
	public Account getAccDetails(@PathVariable String id) {
		return accRepo.findByAccountNo(id);
	}
	

	
}

