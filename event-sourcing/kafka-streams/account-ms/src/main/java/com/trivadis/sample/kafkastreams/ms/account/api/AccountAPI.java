package com.trivadis.sample.kafkastreams.ms.account.api;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.concurrent.CompletableFuture;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Preconditions;

import com.trivadis.sample.kafkastreams.ms.account.aggregate.AccountAggregate;
import com.trivadis.sample.kafkastreams.ms.account.command.AccountCreateCommand;
import com.trivadis.sample.kafkastreams.ms.account.command.DepositMoneyCommand;
import com.trivadis.sample.kafkastreams.ms.account.command.WithdrawMoneyCommand;

@RestController()
@RequestMapping("/api")
public class AccountAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountAPI.class);

    @Autowired
    private AccountAggregate accountAggregate;
    
    @RequestMapping(value= "/accounts",
            method = RequestMethod.POST,
            consumes = "application/json") 
    @Transactional
    public void postCustomer(@RequestBody @Valid AccountCreateCommand command) throws ParseException {
        Preconditions.checkNotNull(command);
        
        accountAggregate.performAccountCreateCommand(command);
    }
    
	@PutMapping(path = "/deposit/{accountId}")
	public void deposit(@RequestBody DepositMoneyCommand command) {
        Preconditions.checkNotNull(command);
        
        accountAggregate.performDepositMoneyCommand(command);
	}

	@PutMapping(path = "/withdraw/{accountId}")
	public void deposit(@RequestBody WithdrawMoneyCommand command) {
        Preconditions.checkNotNull(command);
        
        accountAggregate.performWithdrawMoneyCommand(command);
	}

}