package ankur.revolut.moneytransfer.account.resource;

import ankur.revolut.moneytransfer.Main;
import ankur.revolut.moneytransfer.account.model.AccountRequest;
import ankur.revolut.moneytransfer.account.model.AmountRequest;
import ankur.revolut.moneytransfer.account.model.TransferRequest;
import ankur.revolut.moneytransfer.account.service.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;
import spark.utils.IOUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.security.MessageDigest.getInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccountsControllerTest {

    static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void beforeClass() {
        Main.main(null);

        Spark.awaitInitialization();
    }

    @AfterClass
    public static void afterClass() {
        Spark.stop();
    }

    @Test
    public void newAccountRequest() {
        // given
        AccountRequest req = givenAccountRequest();

        // when
        TestResponse res = request("POST", "/v1/accounts", req);

        // then
        String expected = "{\"reason\":\"Success\",\"status\":201,\"firstName\":\"Ankur\",\"otherName\":\"\",\"surname\":\"Agarwal\",\"accountNumber\":\"000000001\",\"totalAmount\":0.0}";
        assertEquals(201, res.getStatus());
        assertEquals(expected, res.getBody());
    }

    @Test
    public void getAccountRequest() {
        // given
        AccountRequest req = givenAccountRequest();

        TestResponse res = request("POST", "/v1/accounts", req);
        String accountNum = getAccountNumber(res.getBody());

        // when
        res = request("GET", String.format("/v1/accounts/%s", accountNum));

        // then
        String expected = String.format("{\"reason\":\"Success\",\"status\":200,\"firstName\":\"Ankur\"," +
                "\"otherName\":\"\",\"surname\":\"Agarwal\",\"accountNumber\":\"%s\",\"totalAmount\":0.0}", accountNum);
        assertEquals(200, res.getStatus());
        assertEquals(expected, res.getBody());
    }

    @Test
    public void getAccountRequestFailsNoAccount() {
        // given
        AccountRequest req = givenAccountRequest();

        // when
        TestResponse res = request("GET", "/v1/accounts/999999999");

        // then
        assertEquals(404, res.getStatus());
    }

    @Test
    public void getAccountAmountRequest() {
        // given
        AccountRequest req = givenAccountRequest();

        TestResponse res = request("POST", "/v1/accounts", req);
        String accountNum = getAccountNumber(res.getBody());

        // when
        res = request("GET", String.format("/v1/accounts/%s/money", accountNum));

        // then
        String expected = "{\"reason\":\"Success\",\"status\":200,\"totalAmount\":0.0}";
        assertEquals(200, res.getStatus());
        assertEquals(expected, res.getBody());
    }

    @Test
    public void addAmountToAccountRequest() {
        // given
        AccountRequest req = givenAccountRequest();
        AmountRequest req2 = givenAmountRequest(10);

        TestResponse res = request("POST", "/v1/accounts", req);
        String accountNum = getAccountNumber(res.getBody());

        // when
        res = request("PATCH", String.format("/v1/accounts/%s/money", accountNum), req2);

        // then
        String expected = "{\"reason\":\"Funds added\",\"status\":200,\"totalAmount\":10.0}";
        assertEquals(200, res.getStatus());
        assertEquals(expected, res.getBody());
    }

    @Test
    public void ensureLocksWorkingWhenAddingToAccount() {
        // given
        AccountRequest req = givenAccountRequest();

        TestResponse res = request("POST", "/v1/accounts", req);
        String accountNum = getAccountNumber(res.getBody());
        int numThreads = 50;
        int numRequests = 400;

        // when
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger totalResponses = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        IntStream.range(0, numRequests)
                .forEach(i -> executor.submit(() -> {
                    AmountRequest req2 = givenAmountRequest(2);
                    TestResponse res2 = request("PATCH", String.format("/v1/accounts/%s/money", accountNum), req2);
                    totalResponses.incrementAndGet();
                    if (res2.getStatus() == 200) {
                        total.addAndGet(2);
                    } else {
                        System.out.println(String.format("%d response", res2.getStatus()));
                    }
                }));

        shutdownExecutor(executor);

        res = request("GET", String.format("/v1/accounts/%s/money", accountNum));

        // then
        System.out.println(total.get());
        System.out.println(res.getBody());
        assertEquals(numRequests, totalResponses.get());
        assertTrue(res.getBody().contains(String.format(":%d", total.get())));
    }

    @Test
    public void addAmountToAccountRequestFailedDueToRequestAlreadyComplete() {
        // given
        AccountRequest req = givenAccountRequest();
        AmountRequest req2 = givenAmountRequest(10);

        TestResponse res = request("POST", "/v1/accounts", req);
        String accountNum = getAccountNumber(res.getBody());

        // when
        request("PATCH", String.format("/v1/accounts/%s/money", accountNum), req2);
        res = request("PATCH", String.format("/v1/accounts/%s/money", accountNum), req2);

        // then
        assertEquals(409, res.getStatus());
    }

    @Test
    public void transferFundsRequest() {
        // given
        AccountRequest req = givenAccountRequest();
        AmountRequest req2 = givenAmountRequest(100);

        TestResponse res = request("POST", "/v1/accounts", req);
        String accountNumA = getAccountNumber(res.getBody());
        res = request("POST", "/v1/accounts", req);
        String accountNumB = getAccountNumber(res.getBody());

        TransferRequest req3 = givenTransferRequest(65, accountNumB);

        // when
        request("PATCH", String.format("/v1/accounts/%s/money", accountNumA), req2);
        res = request("PATCH", String.format("/v1/accounts/%s/money/transfer", accountNumA), req3);

        // then
        String expected = "{\"reason\":\"Funds added\",\"status\":200,\"totalAmount\":35.0}";
        assertEquals(200, res.getStatus());
        assertEquals(expected, res.getBody());
    }

    @Test
    public void addAmountToAccountRequestFailWhenUsingSameAccountToTransferBetween() {
        // given
        AccountRequest req = givenAccountRequest();
        AmountRequest req2 = givenAmountRequest(100);

        TestResponse res = request("POST", "/v1/accounts", req);
        String accountNumA = getAccountNumber(res.getBody());
        request("PATCH", String.format("/v1/accounts/%s/money", accountNumA), req2);

        TransferRequest req3 = givenTransferRequest(65, accountNumA);

        // when
        res = request("PATCH", String.format("/v1/accounts/%s/money/transfer", accountNumA), req3);

        // then
        assertEquals(400, res.getStatus());
    }

    @Test
    public void transferFundsRequestFailedDueToInsufficientFunds() {
        // given
        AccountRequest req = givenAccountRequest();
        AmountRequest req2 = givenAmountRequest(100);

        TestResponse res = request("POST", "/v1/accounts", req);
        String accountNumA = getAccountNumber(res.getBody());
        res = request("POST", "/v1/accounts", req);
        String accountNumB = getAccountNumber(res.getBody());

        TransferRequest req3 = givenTransferRequest(1000, accountNumB);

        // when
        request("PATCH", String.format("/v1/accounts/%s/money", accountNumA), req2);
        res = request("PATCH", String.format("/v1/accounts/%s/money/transfer", accountNumA), req3);

        // then
        assertEquals(400, res.getStatus());
    }

    @Test
    public void ensureLocksWorkingWhenTransferringBetweenAccountsAndNoDeadLock() {
        // given
        AccountRequest req = givenAccountRequest();
        AmountRequest req2 = givenAmountRequest(1000);

        TestResponse res = request("POST", "/v1/accounts", req);
        String accountNumA = getAccountNumber(res.getBody());
        res = request("POST", "/v1/accounts", req);
        String accountNumB = getAccountNumber(res.getBody());
        request("PATCH", String.format("/v1/accounts/%s/money", accountNumA), req2);
        request("PATCH", String.format("/v1/accounts/%s/money", accountNumB), req2);
        int numThreads = 50;
        int numRequests = 500;

        // when
        AtomicInteger totalResponses = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        IntStream.range(0, numRequests)
                .forEach(i -> executor.submit(() -> {
                    String account1 = accountNumA;
                    String account2 = accountNumB;
                    if (i % 2 == 0) {
                        account1 = accountNumB;
                        account2 = accountNumA;
                    }
                    TransferRequest req3 = givenTransferRequest(2, account2);
                    TestResponse res2 = request("PATCH", String.format("/v1/accounts/%s/money/transfer", account1), req3);
                    totalResponses.incrementAndGet();
                    if (res2.getStatus() > 299) {
                        System.out.println(String.format("%d response", res2.getStatus()));
                    }
                }));

        shutdownExecutor(executor);

        // then
        assertEquals(numRequests, totalResponses.get());
    }

    private void shutdownExecutor(ExecutorService executor) {
        try {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("termination interrupted");
        } finally {
            if (!executor.isTerminated()) {
                System.err.println("killing non-finished tasks");
            }
            executor.shutdownNow();
        }
    }

    private String getAccountNumber(String body) {
        String find = "accountNumber\":\"";
        int index = body.indexOf(find);
        return body.substring(index + find.length(), index + find.length() + 9);
    }

    private AccountRequest givenAccountRequest() {
        AccountRequest req = new AccountRequest();
        req.setFirstName("Ankur");
        req.setOtherName("");
        req.setSurname("Agarwal");
        return req;
    }

    private AmountRequest givenAmountRequest(float amount) {
        AmountRequest req = new AmountRequest();
        req.setAmount(amount);
        req.setRequestID(hashString(String.valueOf(amount)));
        return req;
    }

    private TransferRequest givenTransferRequest(float amount, String accountNumB) {
        TransferRequest req = new TransferRequest();
        req.setAmount(amount);
        req.setTo(accountNumB);
        req.setRequestID(hashString(String.valueOf(amount)));
        return req;
    }

    private String hashString(String value) {
        MessageDigest MD5 = null;
        try {
            MD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String newValue = value + OffsetDateTime.now().toString();
        return DatatypeConverter.printHexBinary(MD5.digest(newValue.getBytes()));
    }

    private static TestResponse request(String method, String path) {
        try {
            URL url = new URL("http://localhost:8080" + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            if (connection.getResponseCode() > 299 ) {
                connection.disconnect();
                return new TestResponse(connection.getResponseCode(), "");
            } else {
                InputStream in = new BufferedInputStream(connection.getInputStream());
                String body = IOUtils.toString(in);
                in.close();
                connection.disconnect();
                return new TestResponse(connection.getResponseCode(), body);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            String test = ex.getMessage();
            return new TestResponse(500, "Test error");
        }
    }

    private static TestResponse request(String method, String path, Object requestBody) {
        try {
            URL url = new URL("http://localhost:8080" + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (method == "PATCH") {
                connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                connection.setRequestMethod("POST");
            } else {
                connection.setRequestMethod(method);
            }
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            byte[] input = objectMapper.writeValueAsBytes(requestBody);
            os.write(input, 0, input.length);
            os.close();

            if (connection.getResponseCode() > 299 ) {
                connection.disconnect();
                return new TestResponse(connection.getResponseCode(), "");
            } else {
                InputStream in = new BufferedInputStream(connection.getInputStream());
                String body = IOUtils.toString(in);
                in.close();
                connection.disconnect();
                return new TestResponse(connection.getResponseCode(), body);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return new TestResponse(500, "Test error");
        }
    }

    private static class TestResponse {

        String body;
        int status;

        public TestResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }

        public String getBody() {
            return body;
        }

        public int getStatus() {
            return status;
        }
    }
}
