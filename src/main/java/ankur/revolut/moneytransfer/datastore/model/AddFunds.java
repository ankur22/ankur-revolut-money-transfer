package ankur.revolut.moneytransfer.datastore.model;

public class AddFunds {
    private final String accountNumber;
    private final Amount amount;
    private final String requestID;

    public AddFunds(String accountNumber, Amount amount, String requestID) {
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.requestID = requestID;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getRequestID() {
        return requestID;
    }
}
