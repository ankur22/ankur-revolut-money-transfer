package ankur.revolut.moneytransfer.account.model;

public class AmountRequest {
    private float amount;
    private String requestID;

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }
}
