package ankur.revolut.moneytransfer.account.resource;

import ankur.revolut.moneytransfer.account.model.AccountRequest;
import ankur.revolut.moneytransfer.account.model.AmountRequest;
import ankur.revolut.moneytransfer.account.model.HttpResponse;
import ankur.revolut.moneytransfer.account.model.TransferRequest;
import ankur.revolut.moneytransfer.account.service.AccountsService;
import ankur.revolut.moneytransfer.account.service.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;

import java.util.Optional;

import static spark.Spark.*;

public class AccountsController {

    private static final Logger log = LoggerFactory.getLogger(AccountsController.class);

    public AccountsController(AccountsService accountsService, ObjectMapper objectMapper) {

        post("/v1/accounts", (request, response) -> {
            log.info("Request to add account received");
            Optional<AccountRequest> accountRequest = objectMapper.tryReadValue(request.body(), AccountRequest.class);
            if (accountRequest.isPresent()) {
                HttpResponse r = accountsService.addNewAccount(accountRequest.get());
                response.status(r.getStatus());
                return objectMapper.writeValueAsString(r);
            } else {
                return malformedRequestBody(objectMapper, response);
            }
        });

        get("/v1/accounts/:accountNumber", (request, response) -> {
            log.info("Request to get account details received");
            String accountNumber = request.params(":accountNumber");
            HttpResponse r = accountsService.getAccount(accountNumber);
            response.status(r.getStatus());
            return objectMapper.writeValueAsString(r);
        });

        get("/v1/accounts/:accountNumber/money", (request, response) -> {
            log.info("Request to get account money details received");
            String accountNumber = request.params(":accountNumber");
            HttpResponse r = accountsService.getAmount(accountNumber);
            response.status(r.getStatus());
            return objectMapper.writeValueAsString(r);
        });

        patch("/v1/accounts/:accountNumber/money", (request, response) -> {
            log.info("Request to add money to account received");
            String accountNumber = request.params(":accountNumber");
            Optional<AmountRequest> accountRequest = objectMapper.tryReadValue(request.body(), AmountRequest.class);
            if (accountRequest.isPresent()) {
                HttpResponse r = accountsService.addAmount(accountNumber, accountRequest.get());
                response.status(r.getStatus());
                return objectMapper.writeValueAsString(r);
            } else {
                return malformedRequestBody(objectMapper, response);
            }
        });

        patch("/v1/accounts/:accountNumber/money/transfer", (request, response) -> {
            log.info("Request to transfer money from account to another account received");
            String accountNumber = request.params(":accountNumber");
            Optional<TransferRequest> transferRequest = objectMapper.tryReadValue(request.body(), TransferRequest.class);
            if (transferRequest.isPresent()) {
                HttpResponse r = accountsService.transferFunds(accountNumber, transferRequest.get());
                response.status(r.getStatus());
                return objectMapper.writeValueAsString(r);
            } else {
                return malformedRequestBody(objectMapper, response);
            }
        });
    }

    private String malformedRequestBody(ObjectMapper objectMapper, Response response) {
        try {
            response.status(400);
            return objectMapper.writeValueAsString(new HttpResponse("Unrecognised request body", 400));
        } catch (JsonProcessingException ex) {
            log.error("Exception while attempting to write a error response", ex);
            response.status(500);
            return "{\"reason:\":\"Unexpected error\"}";
        }
    }
}
