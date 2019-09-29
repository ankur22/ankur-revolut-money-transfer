package ankur.revolut.moneytransfer.datastore;

import ankur.revolut.moneytransfer.datastore.model.Account;
import ankur.revolut.moneytransfer.datastore.model.AddFunds;
import ankur.revolut.moneytransfer.datastore.model.Amount;
import ankur.revolut.moneytransfer.datastore.model.Transfer;

import java.util.Optional;

public interface AccountDao {
	Optional<Account> addAccount(Account account);
	Optional<Account> getAccount(String accountNumber);
	Optional<Amount> getAmount(String accountNumber);
	boolean doesAccountExist(String accountNumber);
	FundEnum addFunds(AddFunds addFunds);
	FundEnum transferFunds(Transfer transfer);
}
