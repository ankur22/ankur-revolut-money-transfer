package ankur.revolut.moneytransfer.account.service;

import ankur.revolut.moneytransfer.account.model.*;
import ankur.revolut.moneytransfer.datastore.AccountDao;
import ankur.revolut.moneytransfer.datastore.FundEnum;
import ankur.revolut.moneytransfer.datastore.model.Account;
import ankur.revolut.moneytransfer.datastore.model.AddFunds;
import ankur.revolut.moneytransfer.datastore.model.Amount;
import ankur.revolut.moneytransfer.datastore.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class AccountsService {

    private static final Logger log = LoggerFactory.getLogger(AccountsService.class);

    private AccountDao accountDao;
    private AccountNumberCreator accountNumberCreator;
    private Transformer transformer;

    public static AccountsService create(AccountDao accountDao, AccountNumberCreator accountNumberCreator,
                                         Transformer transformer) {
        return new AccountsService(accountDao, accountNumberCreator, transformer);
    }

    private AccountsService(AccountDao accountDao, AccountNumberCreator accountNumberCreator, Transformer transformer) {
        this.accountDao = accountDao;
        this.accountNumberCreator = accountNumberCreator;
        this.transformer = transformer;
    }

    public HttpResponse addNewAccount(AccountRequest accountRequest) {
        String newAccountNumber = accountNumberCreator.createAccountNumber();
        Account newAccount = transformer.addAccountRequest(accountRequest, newAccountNumber,
                                                            OffsetDateTime.now(ZoneOffset.UTC));

        Optional<Account> account = accountDao.addAccount(newAccount);
        if (account.isPresent()) {
            Optional<Amount> amount = accountDao.getAmount(account.get().getAccountNumber());
            if (amount.isPresent()) {
                return transformer.addNewAccountResponse(account.get(), amount.get());
            } else {
                log.error("New account with no amount");
            }
        }

        return transformer.addAccountResponseError();
    }

    public HttpResponse getAccount(String accountNumber) {
        Optional<Account> account = accountDao.getAccount(accountNumber);
        if (account.isPresent()) {
            Optional<Amount> amount = accountDao.getAmount(account.get().getAccountNumber());
            if (amount.isPresent()) {
                return transformer.getAccountResponse(account.get(), amount.get());
            } else {
                log.error("Found account with no amount");
            }
        }

        return transformer.noAccountResponseError();
    }

    public HttpResponse getAmount(String accountNumber) {
        Optional<Amount> amount = accountDao.getAmount(accountNumber);
        if (amount.isPresent()) {
            return transformer.addAmountResponse(amount.get());
        } else {
            log.error("Found account with no amount");
        }

        return transformer.noAccountResponseError();
    }

    public HttpResponse addAmount(String accountNumber, AmountRequest amountRequest) {
        AddFunds addFunds = transformer.addAmountRequest(amountRequest, accountNumber);
        FundEnum response = accountDao.addFunds(addFunds);
        Optional<Amount> amount = accountDao.getAmount(accountNumber);
        if (amount.isPresent()) {
            return transformer.addAmountResponse(amountRequest.getRequestID(), response, amount.get());
        } else {
            log.error("No amount for account or invalid accountID received");
            return transformer.addAmountResponse(amountRequest.getRequestID(), response);
        }
    }

    public HttpResponse transferFunds(String accountNumber, TransferRequest transferRequest) {
        Transfer transfer = transformer.transferAmountRequest(transferRequest, accountNumber);
        if (accountNumber.equals(transfer.getToAccountB().getAccountNumber())) {
            log.info(String.format("%s - transfer within same account is not allowed", transferRequest));
            return transformer.transferResponse(transferRequest.getRequestID(), FundEnum.SAME_ACCOUNT);
        }
        FundEnum response = accountDao.transferFunds(transfer);
        Optional<Amount> amount = accountDao.getAmount(accountNumber);
        if (amount.isPresent()) {
            return transformer.transferResponse(transferRequest.getRequestID(), response, amount.get());
        } else {
            log.error("No amount for account or invalid accountID received");
            return transformer.transferResponse(transferRequest.getRequestID(), response);
        }
    }
}
