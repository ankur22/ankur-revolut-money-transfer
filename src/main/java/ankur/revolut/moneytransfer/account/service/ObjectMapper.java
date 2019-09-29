package ankur.revolut.moneytransfer.account.service;

import java.util.Optional;

public class ObjectMapper extends com.fasterxml.jackson.databind.ObjectMapper {

    public <T> Optional<T> tryReadValue(String content, Class<T> valueType) {
        try {
            return Optional.of(super.readValue(content, valueType));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
