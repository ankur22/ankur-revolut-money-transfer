package ankur.revolut.moneytransfer.datastore;

import ankur.revolut.moneytransfer.datastore.model.Account;
import ankur.revolut.moneytransfer.datastore.model.AddFunds;
import ankur.revolut.moneytransfer.datastore.model.Amount;
import ankur.revolut.moneytransfer.datastore.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryAccountDao implements AccountDao {

    private static final Logger log = LoggerFactory.getLogger(InMemoryAccountDao.class);

    ConcurrentHashMap<String, Account> accountNumbers = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Amount> accountAmounts = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, ConcurrentHashMap<String, Amount>> transactionAudit = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    long lockAcquireTimeout = 0;

    public static InMemoryAccountDao newWithTimeout(long lockAcquireTimeout) {
        return new InMemoryAccountDao(lockAcquireTimeout);
    }

    private InMemoryAccountDao(long lockAcquireTimeout) {
        this.lockAcquireTimeout = lockAcquireTimeout;
    }

    @Override
    public Optional<Account> addAccount(Account account) {
        if (accountNumbers.containsKey(account.getAccountNumber())) {
            return Optional.empty();
        }

        if (accountAmounts.containsKey(account.getAccountNumber())
                || transactionAudit.containsKey(account.getAccountNumber())
                || locks.containsKey(account.getAccountNumber())) {
            log.error("Account number found in other data structures when creating new account.");
            return Optional.empty();
        }

        accountNumbers.put(account.getAccountNumber(), account);
        accountAmounts.put(account.getAccountNumber(), Amount.ZERO);
        transactionAudit.put(account.getAccountNumber(), new ConcurrentHashMap<>());
        locks.put(account.getAccountNumber(), new ReentrantLock());

        log.info("Created new account");

        return Optional.of(account);
    }

    @Override
    public Optional<Account> getAccount(String accountNumber) {
        return Optional.ofNullable(accountNumbers.get(accountNumber));
    }

    @Override
    public Optional<Amount> getAmount(String accountNumber) {
        if (!doesAccountExist(accountNumber)) {
            return Optional.empty();
        }

        if (!accountAmounts.containsKey(accountNumber)) {
            log.error(String.format("Expecting account in internal data structures, but not found"));
            return Optional.empty();
        }

        return Optional.of(accountAmounts.get(accountNumber));
    }

    @Override
    public boolean doesAccountExist(String accountNumber) {
        return accountNumbers.containsKey(accountNumber);
    }

    @Override
    public FundEnum addFunds(AddFunds addFunds) {
        FundEnum response = dataStorePreCheckBeforeFundChangesSrc(addFunds);
        if (response != FundEnum.ACCOUNT_PRE_CHECK_PASS) {
            return response;
        }

        FundEnum returnValue = FundEnum.TIMED_OUT;
        ReentrantLock accountLock = locks.get(addFunds.getAccountNumber());

        try {
            if (accountLock.tryLock(lockAcquireTimeout, TimeUnit.MILLISECONDS)) {
                Amount oldAmount = accountAmounts.get(addFunds.getAccountNumber());

                FundEnum overUnderCheck = fundChangeCausesOverflowOrUnderflow(oldAmount, addFunds.getAmount());
                if (overUnderCheck != FundEnum.FUND_NOT_OVER_UNDER_FLOW) {
                    returnValue = overUnderCheck;
                } else {
                    performFundTransaction(addFunds, oldAmount);
                    returnValue = FundEnum.SUCCESS;
                    log.info(String.format("%s - transaction completed successfully", addFunds.getRequestID()));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.warn(String.format("%s - Thread interrupted during lock acquisition", addFunds.getRequestID()));
        } finally {
            if (accountLock.isHeldByCurrentThread()) {
                accountLock.unlock();
            }
        }

        if (returnValue == FundEnum.TIMED_OUT) {
            log.info(String.format("%s - Lock acquisition may have timed out", addFunds.getRequestID()));
        }

        return returnValue;
    }

    private void performFundTransaction(AddFunds addFunds, Amount oldAmount) {
        Amount newAmount = Amount.newAmount(addFunds.getAmount().getValue() + oldAmount.getValue());
        accountAmounts.put(addFunds.getAccountNumber(), newAmount);

        ConcurrentHashMap<String, Amount> accountAudit = transactionAudit.get(addFunds.getAccountNumber());
        accountAudit.put(addFunds.getRequestID(), addFunds.getAmount());
    }

    @Override
    public FundEnum transferFunds(Transfer transfer) {
        FundEnum response = dataStorePreCheckBeforeFundChangesSrc(transfer.getFromAccountA());
        if (response != FundEnum.ACCOUNT_PRE_CHECK_PASS) {
            return response;
        }
        response = dataStorePreCheckBeforeFundChangesDest(transfer.getToAccountB());
        if (response != FundEnum.ACCOUNT_PRE_CHECK_PASS) {
            return response;
        }

        FundEnum returnValue = FundEnum.TIMED_OUT;

        ReentrantLock accountLockA = locks.get(transfer.getFromAccountA().getAccountNumber());
        ReentrantLock accountLockB = locks.get(transfer.getToAccountB().getAccountNumber());

        try {
            if (accountLockA.tryLock(lockAcquireTimeout, TimeUnit.MILLISECONDS)
                    && accountLockB.tryLock(lockAcquireTimeout, TimeUnit.MILLISECONDS)){

                Amount oldAmountA = accountAmounts.get(transfer.getFromAccountA().getAccountNumber());
                Amount oldAmountB = accountAmounts.get(transfer.getToAccountB().getAccountNumber());

                FundEnum overUnderCheckA = fundChangeCausesOverflowOrUnderflow(oldAmountA, transfer.getFromAccountA().getAmount());
                FundEnum overUnderCheckB = fundChangeCausesOverflowOrUnderflow(oldAmountB, transfer.getToAccountB().getAmount());

                if (overUnderCheckA != FundEnum.FUND_NOT_OVER_UNDER_FLOW) {
                    returnValue = overUnderCheckA;
                } else if (overUnderCheckB != FundEnum.FUND_NOT_OVER_UNDER_FLOW) {
                    returnValue = overUnderCheckB;
                } else {
                    performFundTransaction(transfer.getFromAccountA(), oldAmountA);
                    performFundTransaction(transfer.getToAccountB(), oldAmountB);
                    returnValue = FundEnum.SUCCESS;
                    log.info(String.format("%s - transaction completed successfully", transfer.getFromAccountA().getRequestID()));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.warn(String.format("%s - Thread interrupted during lock acquisition", transfer.getFromAccountA().getRequestID()));
        } finally {
            if (accountLockB.isHeldByCurrentThread()) {
                accountLockB.unlock();
            }

            if (accountLockA.isHeldByCurrentThread()) {
                accountLockA.unlock();
            }
        }

        if (returnValue == FundEnum.TIMED_OUT) {
            log.info(String.format("%s - Lock acquisition may have timed out", transfer.getFromAccountA().getRequestID()));
        }

        return returnValue;
    }

    private FundEnum fundChangeCausesOverflowOrUnderflow(Amount oldAmount, Amount amountChange) {
        if (amountChange.getValue() > 0) {
            if (Float.MAX_VALUE - amountChange.getValue() <= oldAmount.getValue()) {
                return FundEnum.FUND_OVERFLOW;
            }
        } else {
            if (oldAmount.getValue() + amountChange.getValue() < 0) {
                return FundEnum.FUND_UNDERFLOW;
            }
        }
        return FundEnum.FUND_NOT_OVER_UNDER_FLOW;
    }

    private FundEnum dataStorePreCheckBeforeFundChangesSrc(AddFunds addFunds) {
        if (!doesAccountExist(addFunds.getAccountNumber())) {
            log.info(String.format("%s - Account not found when attempting to add funds", addFunds.getRequestID()));
            return FundEnum.ACCOUNT_NOT_FOUND;
        }
        return dataStorePreCheckBeforeFundChanges(addFunds.getAccountNumber(), addFunds.getRequestID());
    }

    private FundEnum dataStorePreCheckBeforeFundChangesDest(AddFunds addFunds) {
        if (!doesAccountExist(addFunds.getAccountNumber())) {
            log.info(String.format("%s - Account not found when attempting to add funds", addFunds.getRequestID()));
            return FundEnum.DEST_ACCOUNT_NOT_FOUND;
        }
        return dataStorePreCheckBeforeFundChanges(addFunds.getAccountNumber(), addFunds.getRequestID());
    }

    private FundEnum dataStorePreCheckBeforeFundChanges(String accountNumber, String requestID) {
        if (!transactionAudit.containsKey(accountNumber)
                || !locks.containsKey(accountNumber)
                || !accountAmounts.containsKey(accountNumber)) {
            log.error(String.format("%s - Expecting account in internal data structures, but not found", requestID));
            return FundEnum.ACCOUNT_NOT_FOUND;
        }

        ConcurrentHashMap<String, Amount> accountAudit = transactionAudit.get(accountNumber);
        if (accountAudit.containsKey(requestID)) {
            log.info(String.format("%s - Transaction already completed", requestID));
            return FundEnum.TRANSACTION_ALREADY_COMPLETE;
        }

        log.info(String.format("%s - Pre check before fund changes passed", requestID));

        return FundEnum.ACCOUNT_PRE_CHECK_PASS;
    }
}
