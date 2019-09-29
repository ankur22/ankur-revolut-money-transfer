package ankur.revolut.moneytransfer.datastore;

import ankur.revolut.moneytransfer.datastore.AccountDao;

public class AccountDaoCreator {

    public static AccountDao createDao(long lockAcquireTimeout) {
        return InMemoryAccountDao.newWithTimeout(lockAcquireTimeout);
    }
}
