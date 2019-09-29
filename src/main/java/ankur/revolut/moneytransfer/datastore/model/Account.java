package ankur.revolut.moneytransfer.datastore.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Account {

    private final String firstName;
    private final String otherName;
    private final String surname;
    private final String accountNumber;
    private final OffsetDateTime creationDateTime;

    Account(String firstName, String otherName, String surname, String accountNumber, OffsetDateTime creationDateTime) {
        this.firstName = firstName;
        this.otherName = otherName;
        this.surname = surname;
        this.accountNumber = accountNumber;
        this.creationDateTime = creationDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return firstName.equals(account.firstName) &&
                otherName.equals(account.otherName) &&
                surname.equals(account.surname) &&
                accountNumber.equals(account.accountNumber) &&
                creationDateTime.equals(account.creationDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, otherName, accountNumber, surname, creationDateTime);
    }

    @Override
    public String toString() {
        return "Account{" +
                "firstName='" + firstName + '\'' +
                ", otherName='" + otherName + '\'' +
                ", surname='" + surname + '\'' +
                ", accountNumber=" + accountNumber + '\'' +
                ", creationDateTime=" + creationDateTime +
                '}';
    }

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

    public OffsetDateTime getCreationDateTime() {
        return creationDateTime;
    }


    public static final class AccountBuilder {
        private String firstName;
        private String otherName;
        private String surname;
        private String accountNumber;
        private OffsetDateTime creationDateTime;

        private AccountBuilder() {
        }

        public static AccountBuilder anAccount() {
            return new AccountBuilder();
        }

        public AccountBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public AccountBuilder withOtherName(String otherName) {
            this.otherName = otherName;
            return this;
        }

        public AccountBuilder withSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public AccountBuilder withAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public AccountBuilder withCreationDateTime(OffsetDateTime creationDateTime) {
            this.creationDateTime = creationDateTime;
            return this;
        }

        public Account build() {
            return new Account(firstName, otherName, surname, accountNumber, creationDateTime);
        }
    }
}

