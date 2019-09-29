package ankur.revolut.moneytransfer.account.model;

public class HttpResponse {

    private final String reason;
    private final int status;

    public HttpResponse(String reason, int status) {
        this.reason = reason;
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public int getStatus() {
        return status;
    }


    public static final class HttpResponseBuilder {
        private String reason;
        private int status;

        private HttpResponseBuilder() {
        }

        public static HttpResponseBuilder aHttpResponse() {
            return new HttpResponseBuilder();
        }

        public HttpResponseBuilder withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public HttpResponseBuilder withStatus(int status) {
            this.status = status;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(reason, status);
        }
    }
}
