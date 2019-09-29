package ankur.revolut.moneytransfer.account.service;

import ankur.revolut.moneytransfer.account.model.*;
import ankur.revolut.moneytransfer.datastore.FundEnum;
import ankur.revolut.moneytransfer.datastore.model.Account;
import ankur.revolut.moneytransfer.datastore.model.AddFunds;
import ankur.revolut.moneytransfer.datastore.model.Amount;
import ankur.revolut.moneytransfer.datastore.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Optional;

public class HTTPTransformer implements Transformer {

    private static final Logger log = LoggerFactory.getLogger(HTTPTransformer.class);

    @Override
    public Account addAccountRequest(AccountRequest request, String accountNumber, OffsetDateTime now) {
        return Account.AccountBuilder.anAccount()
                                    .withAccountNumber(accountNumber)
                                    .withCreationDateTime(now)
                                    .withFirstName(request.getFirstName())
                                    .withOtherName(request.getOtherName())
                                    .withSurname(request.getSurname()).build();
    }

    @Override
    public HttpResponse addAccountResponseError() {
        return new HttpResponse("Please try again. Unexpected conflict.", 500);
    }

    @Override
    public HttpResponse noAccountResponseError() {
        return accountNotFound();
    }

    @Override
    public HttpResponse addNewAccountResponse(Account account, Amount amount) {
        return addAccountResponse(account, amount, 201);
    }

    @Override
    public HttpResponse getAccountResponse(Account account, Amount amount) {
        return addAccountResponse(account, amount, 200);
    }

    private HttpResponse addAccountResponse(Account account, Amount amount, int status) {
        return AccountHttpResponse.AccountHttpResponseBuilder
                .anAccountHttpResponse()
                .withAccountNumber(account.getAccountNumber())
                .withTotalAmount(amount.getValue())
                .withFirstName(account.getFirstName())
                .withOtherName(account.getOtherName())
                .withReason("Success")
                .withStatus(status)
                .withSurname(account.getSurname())
                .build();
    }

    @Override
    public HttpResponse addAmountResponse(Amount amount) {
        return AccountHttpResponse.AccountHttpResponseBuilder
                .anAccountHttpResponse()
                .withTotalAmount(amount.getValue())
                .withReason("Success")
                .withStatus(200)
                .build();
    }

    @Override
    public AddFunds addAmountRequest(AmountRequest amountRequest, String accountNumber) {
        return new AddFunds(accountNumber, Amount.newAmount(amountRequest.getAmount()), amountRequest.getRequestID());
    }

    @Override
    public HttpResponse addAmountResponse(String requestID, FundEnum fundEnum, Amount amount) {
        HttpResponse response = addAmountResponse(requestID, fundEnum);

        return AccountHttpResponse.AccountHttpResponseBuilder
                                    .anAccountHttpResponse()
                                    .withTotalAmount(amount.getValue())
                                    .withReason(response.getReason())
                                    .withStatus(response.getStatus())
                                    .build();
    }

    @Override
    public HttpResponse addAmountResponse(String requestID, FundEnum fundEnum) {
        Optional<HttpResponse> response = sharedEnumResponse(fundEnum);
        if (response.isPresent()) {
            return response.get();
        }

        log.error(String.format("%s - %s unknown enum when adding funds", fundEnum));
        return new HttpResponse("Unknown error when adding funds", 500);
    }

    @Override
    public Transfer transferAmountRequest(TransferRequest transferRequest, String accountNumber) {
        AddFunds fromA = new AddFunds(accountNumber, Amount.newAmount(transferRequest.getAmount() * -1), transferRequest.getRequestID());
        AddFunds toB = new AddFunds(transferRequest.getTo(), Amount.newAmount(transferRequest.getAmount()), transferRequest.getRequestID());
        return new Transfer(fromA, toB);
    }

    @Override
    public HttpResponse transferResponse(String requestID, FundEnum fundEnum, Amount amount) {
        HttpResponse response = transferResponse(requestID, fundEnum);

        return AccountHttpResponse.AccountHttpResponseBuilder
                .anAccountHttpResponse()
                .withTotalAmount(amount.getValue())
                .withReason(response.getReason())
                .withStatus(response.getStatus())
                .build();
    }

    @Override
    public HttpResponse transferResponse(String requestID, FundEnum fundEnum) {
        Optional<HttpResponse> response = sharedEnumResponse(fundEnum);
        if (response.isPresent()) {
            return response.get();
        }

        switch (fundEnum) {
            case DEST_ACCOUNT_NOT_FOUND:
                return new HttpResponse("Destination account not found", 404);
            case SAME_ACCOUNT:
                return new HttpResponse("Void transaction within same account", 400);
        }

        log.error(String.format("%s - %s unknown enum when adding funds", fundEnum));
        return new HttpResponse("Unknown error when adding funds", 500);
    }

    private Optional<HttpResponse> sharedEnumResponse(FundEnum fundEnum) {
        switch (fundEnum) {
            case SUCCESS:
                return Optional.of(new HttpResponse("Funds added", 200));
            case FUND_OVERFLOW:
                return Optional.of(new HttpResponse("Too much money in account", 400));
            case FUND_UNDERFLOW:
                return Optional.of(new HttpResponse("Not enough funds in account", 400));
            case TIMED_OUT:
                return Optional.of(new HttpResponse("Timed out, please try again", 408));
            case ACCOUNT_NOT_FOUND:
                return Optional.of(accountNotFound());
            case TRANSACTION_ALREADY_COMPLETE:
                return Optional.of(new HttpResponse("Transaction already complete", 409));
        }
        return Optional.empty();
    }

    private HttpResponse accountNotFound() {
        return new HttpResponse("No account with specified accountNumber", 404);
    }
}
