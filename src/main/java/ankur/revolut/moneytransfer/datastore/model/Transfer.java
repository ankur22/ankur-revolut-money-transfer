package ankur.revolut.moneytransfer.datastore.model;

public class Transfer {
    private final AddFunds fromAccountA;
    private final AddFunds toAccountB;

    public Transfer(AddFunds fromAccountA, AddFunds toAccountB) {
        this.fromAccountA = fromAccountA;
        this.toAccountB = toAccountB;
    }

    public AddFunds getFromAccountA() {
        return fromAccountA;
    }

    public AddFunds getToAccountB() {
        return toAccountB;
    }
}
