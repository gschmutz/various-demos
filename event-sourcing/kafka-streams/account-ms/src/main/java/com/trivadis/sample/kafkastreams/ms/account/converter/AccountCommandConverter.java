package com.trivadis.sample.kafkastreams.ms.account.converter;

import java.util.ArrayList;
import java.util.List;

import com.trivadis.avro.command.account.v1.AccountCreateCommand;
import com.trivadis.avro.command.account.v1.AccountTypeEnum;
import com.trivadis.avro.command.account.v1.DepositMoneyCommand;

public class AccountCommandConverter {
	public static List<String> convertFromCS (List<CharSequence> input) {
		List<String> value = new ArrayList<String>();
		
		if (input != null) {
			for (CharSequence cs : input) {
				value.add(input.toString());
			}
		}
		return value;
	}
	
	public static List<CharSequence> convertFromString (List<String> input) {
		List<CharSequence> value = new ArrayList<CharSequence>();
		
		if (input != null) {
			for (String str : input) {
				value.add(str);
			}
		}
		return value;
	}
	
	public static AccountCreateCommand convert (com.trivadis.sample.kafkastreams.ms.account.command.AccountCreateCommand command) {
		AccountCreateCommand avro = new AccountCreateCommand();
		
		avro.setId(command.getId());
		avro.setCustomerId(command.getForCustomerId());
		avro.setAccountType(AccountTypeEnum.valueOf(command.getAccountType()));
		
		return avro;
	}

	
	public static DepositMoneyCommand convert (com.trivadis.sample.kafkastreams.ms.account.command.DepositMoneyCommand command) {
		DepositMoneyCommand avro = new DepositMoneyCommand();
		
		avro.setId(command.getId());
		avro.setAmount(command.getAmount().doubleValue());
		
		return avro;
	}
}
