package ankur.revolut.moneytransfer.account.service;

import ankur.revolut.moneytransfer.account.model.*;
import ankur.revolut.moneytransfer.datastore.FundEnum;
import ankur.revolut.moneytransfer.datastore.model.Account;
import ankur.revolut.moneytransfer.datastore.model.AddFunds;
import ankur.revolut.moneytransfer.datastore.model.Amount;
import ankur.revolut.moneytransfer.datastore.model.Transfer;

import java.time.OffsetDateTime;

public interface Transformer {
    Account addAccountRequest(AccountRequest request, String accountNumber, OffsetDateTime now);
    HttpResponse addAccountResponseError();
    HttpResponse noAccountResponseError();
    HttpResponse addNewAccountResponse(Account account, Amount amount);
    HttpResponse getAccountResponse(Account account, Amount amount);
    HttpResponse addAmountResponse(Amount amount);
    AddFunds addAmountRequest(AmountRequest amountRequest, String accountNumber);
    HttpResponse addAmountResponse(String requestID, FundEnum fundEnum);
    HttpResponse addAmountResponse(String requestID, FundEnum fundEnum, Amount amount);
    Transfer transferAmountRequest(TransferRequest transferRequest, String accountNumber);
    HttpResponse transferResponse(String requestID, FundEnum fundEnum);
    HttpResponse transferResponse(String requestID, FundEnum fundEnum, Amount amount);
}
