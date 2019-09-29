package ankur.revolut.moneytransfer.account.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AccountNumberCreatorTest {

    @Test
    public void createAccountNumber() {
        // given
        AccountNumberCreator numberCreator = new AccountNumberCreator();

        // when
        String newAccountNumber1 = numberCreator.createAccountNumber();
        String newAccountNumber2 = numberCreator.createAccountNumber();

        // then
        assertEquals("000000001", newAccountNumber1);
        assertEquals("000000002", newAccountNumber2);
    }
}