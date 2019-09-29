package ankur.revolut.moneytransfer.account.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountHttpResponse extends HttpResponse {

    private final String firstName;
    private final String otherName;
    private final String surname;
    private final String accountNumber;
    private final float totalAmount;

    public String getFirstName() {
        return firstName;
    }

    public String getOtherName() {
        return otherName;
    }

    public String getSurname() {
        return surname;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    private AccountHttpResponse(String reason, int status, String firstName, String otherName, String surname,
                               String accountNumber, float totalAmount) {
        super(reason, status);
        this.firstName = firstName;
        this.otherName = otherName;
        this.surname = surname;
        this.accountNumber = accountNumber;
        this.totalAmount = totalAmount;
    }

    public static final class AccountHttpResponseBuilder {
        private String reason;
        private int status;
        private String firstName;
        private String otherName;
        private String surname;
        private String accountNumber;
        private float totalAmount;

        private AccountHttpResponseBuilder() {
        }

        public static AccountHttpResponseBuilder anAccountHttpResponse() {
            return new AccountHttpResponseBuilder();
        }

        public AccountHttpResponseBuilder withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public AccountHttpResponseBuilder withStatus(int status) {
            this.status = status;
            return this;
        }

        public AccountHttpResponseBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public AccountHttpResponseBuilder withOtherName(String otherName) {
            this.otherName = otherName;
            return this;
        }

        public AccountHttpResponseBuilder withSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public AccountHttpResponseBuilder withAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public AccountHttpResponseBuilder withTotalAmount(float totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public AccountHttpResponse build() {
            return new AccountHttpResponse(reason, status, firstName, otherName, surname, accountNumber, totalAmount);
        }
    }
}
