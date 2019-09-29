package ankur.revolut.moneytransfer.datastore.model;

import java.util.Currency;
import java.util.Objects;

public class Amount {

    private static Currency GBP = Currency.getInstance("GBP");
    public static Amount ZERO = newAmount(0);

    private final float value;
    private final Currency currency;

    private Amount(float value, Currency currency) {
        this.value = value;
        this.currency = currency;
    }

    public static Amount newAmount(float value) {
        return new Amount(value, GBP);
    }

    public float getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Amount amount = (Amount) o;
        return Float.compare(amount.value, value) == 0 &&
                currency.equals(amount.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, currency);
    }

    @Override
    public String toString() {
        return "Amount{" +
                "value=" + value +
                ", currency=" + currency +
                '}';
    }
}
