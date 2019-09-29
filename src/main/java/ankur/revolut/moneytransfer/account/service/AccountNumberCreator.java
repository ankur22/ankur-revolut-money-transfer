package ankur.revolut.moneytransfer.account.service;

import java.util.concurrent.atomic.AtomicInteger;

public class AccountNumberCreator {

    private AtomicInteger accountsCount = new AtomicInteger(0);

    public String createAccountNumber() {
        int newAccountNum = accountsCount.incrementAndGet();
        return String.format("%09d", newAccountNum);
    }
}
