package ankur.revolut.moneytransfer;

import ankur.revolut.moneytransfer.account.resource.AccountsController;
import ankur.revolut.moneytransfer.account.service.AccountNumberCreator;
import ankur.revolut.moneytransfer.account.service.AccountsService;
import ankur.revolut.moneytransfer.account.service.HTTPTransformer;
import ankur.revolut.moneytransfer.account.service.ObjectMapper;
import ankur.revolut.moneytransfer.datastore.AccountDao;
import ankur.revolut.moneytransfer.datastore.AccountDaoCreator;

import static spark.Spark.port;

public class Main {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static AccountDao accountDao = AccountDaoCreator.createDao(10);
    private static AccountNumberCreator accountNumberCreator = new AccountNumberCreator();
    private static HTTPTransformer httpTransformer = new HTTPTransformer();
    private static AccountsService accountsService = AccountsService.create(accountDao, accountNumberCreator, httpTransformer);

    public static void main(String[] args) {
        setupServer();

        new AccountsController(accountsService, objectMapper);
    }

    private static void setupServer() {
        port(8080);
//        ipAddress("0.0.0.0");
    }
}
