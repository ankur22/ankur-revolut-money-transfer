package ankur.revolut.moneytransfer.datastore;

import ankur.revolut.moneytransfer.datastore.model.Account;
import ankur.revolut.moneytransfer.datastore.model.AddFunds;
import ankur.revolut.moneytransfer.datastore.model.Amount;
import ankur.revolut.moneytransfer.datastore.model.Transfer;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.Assert.*;

public class InMemoryAccountDaoTest {

    private static OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC);
    private static String ACCOUNT_NUM_A = "000000001";
    private static String ACCOUNT_NUM_B = "000000002";
    private static String REQUEST_ID_1 = "b1946ac92492d2347c6235b4d2611184";
    private static String REQUEST_ID_2 = "591785b794601e212b260e25925636fd";
    private static String REQUEST_ID_3 = "891785b794601e212b260e25925636fe";
    private static float DELTA = 0.0001f;

    @Test
    public void addAccountSuccessful() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccount = createAccount("Ankur", "Agarwal");

        // when
        Optional<Account> response = dao.addAccount(newAccount);

        // then
        assertTrue(response.isPresent());
    }

    @Test
    public void addAccountUnsuccessful() throws InterruptedException {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccount = createAccount("Ankur", "Agarwal");

        // when
        Optional<Account> response = dao.addAccount(newAccount);
        response = dao.addAccount(newAccount);

        // then
        assertFalse(response.isPresent());
    }

    @Test
    public void getAccountSuccess() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccount = createAccount("Ankur", "Agarwal");

        // when
        dao.addAccount(newAccount);
        Optional<Account> account = dao.getAccount(ACCOUNT_NUM_A);

        // then
        assertTrue(account.isPresent());
        assertEquals(newAccount, account.get());
    }

    @Test
    public void getAccountFailedWhenItDoesNotExist() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);

        // when
        Optional<Account> account = dao.getAccount(ACCOUNT_NUM_A);

        // then
        assertFalse(account.isPresent());
    }

    @Test
    public void doesAccountExistSuccess() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccount = createAccount("Ankur", "Agarwal");

        // when
        dao.addAccount(newAccount);
        boolean response = dao.doesAccountExist(ACCOUNT_NUM_A);

        // then
        assertTrue(response);
    }

    @Test
    public void doesAccountExistFailureWhenNoAccount() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);

        // when
        boolean response = dao.doesAccountExist(ACCOUNT_NUM_A);

        // then
        assertFalse(response);
    }

    @Test
    public void getAmountSuccess() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccount = createAccount("Ankur", "Agarwal");
        dao.addAccount(newAccount);
        AddFunds addFunds = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(10), REQUEST_ID_1);

        // when
        dao.addFunds(addFunds);
        Optional<Amount> amount1 = dao.getAmount(ACCOUNT_NUM_A);

        // then
        assertEquals(10.f, amount1.get().getValue(), DELTA);
    }

    @Test
    public void getAmountFailNoAccount() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);

        // when
        Optional<Amount> amount1 = dao.getAmount(ACCOUNT_NUM_A);

        // then
        assertFalse(amount1.isPresent());
    }

    @Test
    public void addFundsIsSuccessful() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccount = createAccount("Ankur", "Agarwal");
        dao.addAccount(newAccount);
        AddFunds addFundsA = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(10), REQUEST_ID_1);
        AddFunds addFundsA2 = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(10), REQUEST_ID_2);

        // when
        FundEnum response1 = dao.addFunds(addFundsA);
        Optional<Amount> amount1 = dao.getAmount(ACCOUNT_NUM_A);
        FundEnum response2 = dao.addFunds(addFundsA2);
        Optional<Amount> amount2 = dao.getAmount(ACCOUNT_NUM_A);

        // then
        assertEquals(FundEnum.SUCCESS, response1);
        assertEquals(FundEnum.SUCCESS, response2);
        assertEquals(10.f, amount1.get().getValue(), DELTA);
        assertEquals(20.f, amount2.get().getValue(), DELTA);
    }

    @Test
    public void addFundFailureWhenNoAccount() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        AddFunds addFundsA = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(10), REQUEST_ID_1);

        // when
        FundEnum response = dao.addFunds(addFundsA);

        // then
        assertEquals(FundEnum.ACCOUNT_NOT_FOUND, response);
    }

    @Test
    public void addFundsIsUnsuccessfulDueToSameRequestId() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccount = createAccount("Ankur", "Agarwal");
        dao.addAccount(newAccount);
        AddFunds addFundsA = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(10), REQUEST_ID_1);

        // when
        dao.addFunds(addFundsA);
        FundEnum response = dao.addFunds(addFundsA);

        // then
        assertEquals(FundEnum.TRANSACTION_ALREADY_COMPLETE, response);
    }

    @Test
    public void addFundsIsUnsuccessfulDueToUnderflow() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccount = createAccount("Ankur", "Agarwal");
        dao.addAccount(newAccount);
        AddFunds addFundsA = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(10), REQUEST_ID_1);
        AddFunds addFundsA2 = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(-Float.MAX_VALUE), REQUEST_ID_2);

        // when
        dao.addFunds(addFundsA);
        FundEnum response = dao.addFunds(addFundsA2);

        // then
        assertEquals(FundEnum.FUND_UNDERFLOW, response);
    }

    @Test
    public void addFundsIsUnsuccessfulDueToOverflow() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccount = createAccount("Ankur", "Agarwal");
        dao.addAccount(newAccount);
        AddFunds addFundsA = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(10), REQUEST_ID_1);
        AddFunds addFundsA2 = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(Float.MAX_VALUE), REQUEST_ID_2);

        // when
        dao.addFunds(addFundsA);
        FundEnum response = dao.addFunds(addFundsA2);

        // then
        assertEquals(FundEnum.FUND_OVERFLOW, response);
    }

    @Test
    public void transferFundsSuccess() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccountA = createAccount("Ankur", "Agarwal");
        Account newAccountB = createAccount("Ankur", "Agarwal", ACCOUNT_NUM_B);
        dao.addAccount(newAccountA);
        dao.addAccount(newAccountB);
        AddFunds addFundsA = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(100), REQUEST_ID_1);
        AddFunds addFundsB = new AddFunds(ACCOUNT_NUM_B, Amount.newAmount(2), REQUEST_ID_2);
        Transfer transfer = new Transfer(new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(-65), REQUEST_ID_3),
                                            new AddFunds(ACCOUNT_NUM_B, Amount.newAmount(65), REQUEST_ID_3));

        // when
        dao.addFunds(addFundsA);
        dao.addFunds(addFundsB);
        Optional<Amount> amount_a_1 = dao.getAmount(ACCOUNT_NUM_A);
        dao.transferFunds(transfer);
        Optional<Amount> amount_a_2 = dao.getAmount(ACCOUNT_NUM_A);
        Optional<Amount> amount_b_1 = dao.getAmount(ACCOUNT_NUM_B);

        // then
        assertEquals(100.f, amount_a_1.get().getValue(), DELTA);
        assertEquals(35.f, amount_a_2.get().getValue(), DELTA);
        assertEquals(67.f, amount_b_1.get().getValue(), DELTA);
    }

    @Test
    public void transferFundsFailUnknownDestAccount() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccountA = createAccount("Ankur", "Agarwal");
        dao.addAccount(newAccountA);
        AddFunds addFundsA = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(100), REQUEST_ID_1);
        Transfer transfer = new Transfer(new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(-65), REQUEST_ID_3),
                                            new AddFunds(ACCOUNT_NUM_B, Amount.newAmount(65), REQUEST_ID_3));

        // when
        dao.addFunds(addFundsA);
        FundEnum response = dao.transferFunds(transfer);

        // then
        assertEquals(FundEnum.DEST_ACCOUNT_NOT_FOUND, response);
    }

    @Test
    public void transferFundsFailUnderflow() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccountA = createAccount("Ankur", "Agarwal");
        Account newAccountB = createAccount("Ankur", "Agarwal", ACCOUNT_NUM_B);
        dao.addAccount(newAccountA);
        dao.addAccount(newAccountB);
        AddFunds addFundsA = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(100), REQUEST_ID_1);
        Transfer transfer = new Transfer(new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(-1000), REQUEST_ID_3),
                new AddFunds(ACCOUNT_NUM_B, Amount.newAmount(1000), REQUEST_ID_3));

        // when
        dao.addFunds(addFundsA);
        FundEnum response = dao.transferFunds(transfer);

        // then
        assertEquals(FundEnum.FUND_UNDERFLOW, response);
    }

    @Test
    public void transferFundsFailOverflow() {
        // given
        AccountDao dao = InMemoryAccountDao.newWithTimeout(100);
        Account newAccountA = createAccount("Ankur", "Agarwal");
        Account newAccountB = createAccount("Ankur", "Agarwal", ACCOUNT_NUM_B);
        dao.addAccount(newAccountA);
        dao.addAccount(newAccountB);
        AddFunds addFundsA = new AddFunds(ACCOUNT_NUM_A, Amount.newAmount(100), REQUEST_ID_1);
        AddFunds addFundsB = new AddFunds(ACCOUNT_NUM_B, Amount.newAmount(Float.MAX_VALUE - 50), REQUEST_ID_2);

        // when
        dao.addFunds(addFundsA);
        FundEnum response_b = dao.addFunds(addFundsB);

        // then
        assertEquals(FundEnum.FUND_OVERFLOW, response_b);
    }

    Account createAccount(String firstName, String surname) {
        return Account.AccountBuilder.anAccount()
                                     .withAccountNumber(ACCOUNT_NUM_A)
                                     .withCreationDateTime(NOW)
                                     .withFirstName(firstName)
                                     .withSurname(surname)
                                     .build();
    }

    Account createAccount(String firstName, String surname, String accountNum) {
        return Account.AccountBuilder.anAccount()
                .withAccountNumber(accountNum)
                .withCreationDateTime(NOW)
                .withFirstName(firstName)
                .withSurname(surname)
                .build();
    }
}