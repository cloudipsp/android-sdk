package com.cloudipsp.android;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertPathValidatorException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by vberegovoy on 09.11.15.
 */
public final class Cloudipsp {
    private static final String HOST = BuildConfig.API_HOST;
    private static final String URL_CALLBACK = "http://callback";
    private static final SimpleDateFormat DATE_AND_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
    private static final SSLSocketFactory tlsSocketFactory = Tls12SocketFactory.getInstance();

    static {
        DATE_AND_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final Handler sMain = new Handler(Looper.getMainLooper());

    public final int merchantId;
    private final CloudipspView cloudipspView;

    public Cloudipsp(int merchantId, CloudipspView cloudipspView) {
        this.merchantId = merchantId;
        this.cloudipspView = cloudipspView;
    }

    public interface PayCallback {
        public void onPaidProcessed(Receipt receipt);

        public void onPaidFailure(Exception e);
    }

    public void pay(final Card card, final Order order, final PayCallback callback) {
        if (!card.isValidCard()) {
            throw new IllegalArgumentException("Card should be valid");
        }

        new Thread(new RunnableWithExceptionWrapper(new PayCallback() {
            @Override
            public void onPaidProcessed(final Receipt receipt) {
                sMain.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onPaidProcessed(receipt);
                    }
                });
            }

            @Override
            public void onPaidFailure(final Exception e) {
                sMain.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onPaidFailure(e);
                    }
                });
            }
        }) {
            @Override
            protected void runInTry() throws java.lang.Exception {
                final String token = getToken(order);
                final Checkout checkout = checkout(card, token, order.email);
                final RunnableWithExceptionWrapper orderChecker = new RunnableWithExceptionWrapper(payCallback) {
                    @Override
                    public void runInTry() throws java.lang.Exception {
                        final Receipt receipt = order(token);
                        payCallback.onPaidProcessed(receipt);
                    }
                };

                if (checkout.action == Checkout.WITHOUT_3DS) {
                    orderChecker.run();
                } else {
                    url3ds(checkout, payCallback);
                }
            }
        }).start();
    }

    private abstract static class RunnableWithExceptionWrapper implements Runnable {
        protected final PayCallback payCallback;

        private RunnableWithExceptionWrapper(PayCallback payCallback) {
            this.payCallback = payCallback;
        }

        @Override
        public final void run() {
            try {
                runInTry();
            } catch (CertPathValidatorException | SSLHandshakeException e) {
                payCallback.onPaidFailure(new Exception.NetworkSecurity(e.getMessage()));
            } catch (FileNotFoundException e) {
                payCallback.onPaidFailure(new Exception.ServerInternalError(e));
            } catch (IOException e) {
                e.printStackTrace();
                payCallback.onPaidFailure(new Exception.NetworkAccess(e.getMessage()));
            } catch (Exception e) {
                payCallback.onPaidFailure(e);
            } catch (JSONException e) {
                payCallback.onPaidFailure(new Exception.IllegalServerResponse(e));
            } catch (java.lang.Exception e) {
                payCallback.onPaidFailure(new Exception.Unknown(e));
            }
        }

        protected abstract void runInTry() throws java.lang.Exception;
    }

    private String getToken(Order order) throws java.lang.Exception {
        final TreeMap<String, Object> request = new TreeMap<String, Object>();

        request.put("order_id", order.id);
        request.put("merchant_id", String.valueOf(merchantId));
        request.put("order_desc", order.description);
        request.put("amount", String.valueOf(order.amount));
        request.put("currency", order.currency.name());
        request.put("signature", "button");
        if (!TextUtils.isEmpty(order.productId)) {
            request.put("product_id", order.productId);
        }
        if (!TextUtils.isEmpty(order.paymentSystems)) {
            request.put("payment_systems", order.paymentSystems);
        }
        if (!TextUtils.isEmpty(order.defaultPaymentSystem)) {
            request.put("default_payment_system", order.defaultPaymentSystem);
        }
        if (order.lifetime != -1) {
            request.put("lifetime", order.lifetime);
        }
        if (TextUtils.isEmpty(order.merchantData)) {
            request.put("merchant_data", "[]");
        } else {
            request.put("merchant_data", order.merchantData);
        }
        if (!TextUtils.isEmpty(order.version)) {
            request.put("version", order.version);
        }
        if (!TextUtils.isEmpty(order.serverCallbackUrl)) {
            request.put("server_callback_url", order.serverCallbackUrl);
        }
        if (!TextUtils.isEmpty(order.reservationData)) {
            request.put("reservation_data", order.reservationData);
        }
        if (order.lang != null) {
            request.put("lang", order.lang.name());
        }
        request.put("preauth", order.preauth ? "Y" : "N");
        request.put("required_rectoken", order.requiredRecToken ? "Y" : "N");
        request.put("verification", order.verification ? "Y" : "N");
        request.put("verification_type", order.verificationType.name());
        request.putAll(order.arguments);
        request.put("response_url", URL_CALLBACK);
        request.put("delayed", "N");


        final JSONObject response = call("/api/button", request);

        final String url = response.getString("checkout_url");
        final String token = url.split("token=")[1];
        return token;
    }

    private static class Checkout {
        public static final int WITHOUT_3DS = 0;
        public static final int WITH_3DS = 1;

        public final SendData sendData;
        public final String url;
        public final int action;

        private Checkout(SendData sendData, String url, int action) {
            this.sendData = sendData;
            this.url = url;
            this.action = action;
        }

        private static class SendData {
            public final String md;
            public final String paReq;
            public final String termUrl;

            private SendData(String md, String paReq, String termUrl) {
                this.md = md;
                this.paReq = paReq;
                this.termUrl = termUrl;
            }
        }
    }

    private static Checkout checkout(Card card, String token, String email) throws java.lang.Exception {
        final TreeMap<String, Object> request = new TreeMap<String, Object>();
        request.put("card_number", card.cardNumber);
        request.put("cvv2", card.cvv);
        request.put("expiry_date", String.format("%02d%02d", card.mm, card.yy));
        request.put("payment_system", "card");
        request.put("token", token);
        request.put("email", email);

        final JSONObject response = call("/api/checkout/ajax", request);
        final String url = response.getString("url");
        if (URL_CALLBACK.equals(url)) {
            return new Checkout(null, url, Checkout.WITHOUT_3DS);
        } else {
            final JSONObject sendData = response.getJSONObject("send_data");

            return new Checkout
                    (
                            new Checkout.SendData
                                    (
                                            sendData.getString("MD"),
                                            sendData.getString("PaReq"),
                                            sendData.getString("TermUrl")
                                    ),
                            url,
                            Checkout.WITH_3DS
                    );
        }
    }

    private static Receipt order(String token) throws java.lang.Exception {
        final TreeMap<String, Object> request = new TreeMap<String, Object>();
        request.put("token", token);
        final JSONObject orderData = call("/api/checkout/merchant/order", request).getJSONObject("order_data");
        return parseOrder(orderData);
    }

    private static Receipt parseOrder(JSONObject orderData) throws JSONException {
        Card.Type cardType;
        try {
            cardType = Card.Type.valueOf(orderData.getString("card_type").toUpperCase());
        } catch (IllegalArgumentException e) {
            cardType = Card.Type.UNKNOWN;
        }


        Date recTokenLifeTime;
        try {
            recTokenLifeTime = DATE_AND_FORMAT.parse(orderData.getString("rectoken_lifetime"));
        } catch (java.lang.Exception e) {
            recTokenLifeTime = null;
        }
        final String settlementCcy = orderData.optString("settlement_currency");
        final Currency settlementCcyEnum;
        if (TextUtils.isEmpty(settlementCcy)) {
            settlementCcyEnum = null;
        } else {
            settlementCcyEnum = Currency.valueOf(settlementCcy);
        }
        final String actualCcy = orderData.optString("actual_currency");
        final Currency actualCcyEnum;
        if (TextUtils.isEmpty(actualCcy)) {
            actualCcyEnum = null;
        } else {
            actualCcyEnum = Currency.valueOf(actualCcy);
        }
        Date settlementDate;
        try {
            settlementDate = DATE_FORMAT.parse(orderData.getString("settlement_date"));
        } catch (java.lang.Exception e) {
            settlementDate = null;
        }
        final String verificationStatus = orderData.optString("verification_status");
        final Receipt.VerificationStatus verificationStatusEnum;
        if (TextUtils.isEmpty(verificationStatus)) {
            verificationStatusEnum = null;
        } else {
            verificationStatusEnum = Receipt.VerificationStatus.valueOf(verificationStatus);
        }

        return new Receipt
                (
                        orderData.getString("masked_card"),
                        orderData.getInt("card_bin"),
                        Integer.valueOf(orderData.getString("amount")),
                        orderData.getInt("payment_id"),
                        Currency.valueOf(orderData.getString("currency")),
                        Receipt.Status.valueOf(orderData.getString("order_status")),
                        Receipt.TransationType.valueOf(orderData.getString("tran_type")),
                        orderData.getString("sender_cell_phone"),
                        orderData.getString("sender_account"),
                        cardType,
                        orderData.getString("rrn"),
                        orderData.getString("approval_code"),
                        orderData.getString("response_code"),
                        orderData.getString("product_id"),
                        orderData.getString("rectoken"),
                        recTokenLifeTime,
                        orderData.optInt("reversal_amount", -1),
                        orderData.optInt("settlement_amount", -1),
                        settlementCcyEnum,
                        settlementDate,
                        orderData.optInt("eci", -1),
                        orderData.optInt("fee", -1),
                        orderData.optInt("actual_amount", -1),
                        actualCcyEnum,
                        orderData.optString("payment_system"),
                        verificationStatusEnum,
                        orderData.getString("signature"),
                        orderData
                );
    }

    private static JSONObject call(String path, TreeMap<String, Object> request) throws java.lang.Exception {
        final JSONObject body = new JSONObject();
        try {
            body.put("request", new JSONObject(request));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        final String read = call(HOST + path, body.toString(), "application/json");
//        if (BuildConfig.DEBUG) {
//            Log.i("Cloudipsp", "Read: " + read);
//        }
        return parseResponse(read);
    }

    private static JSONObject parseResponse(String jsonOfResponse) throws java.lang.Exception {
        final JSONObject response = new JSONObject(jsonOfResponse).getJSONObject("response");
        checkResponse(response);
        return response;
    }

    private static void checkResponse(JSONObject response) throws java.lang.Exception {
        if (!response.getString("response_status").equals("success")) {
            throw new Exception.Failure
                    (
                            response.getString("error_message"),
                            response.getInt("error_code"),
                            response.getString("request_id")
                    );
        }
    }

    private void url3ds(Checkout checkout, final PayCallback callback) throws java.lang.Exception {
        final String htmlPageContent;
        final String [] contentType = new String[1];
        final ResponseInterceptor interceptor = new ResponseInterceptor() {
            @Override
            public void onIntercept(HttpURLConnection httpURLConnection) {
                contentType[0] = httpURLConnection.getHeaderField("Content-Type");
            }
        };
        if (TextUtils.isEmpty(checkout.sendData.paReq)) {
            final JSONObject sendData = new JSONObject();
            sendData.put("MD", checkout.sendData.md);
            sendData.put("PaReq", checkout.sendData.paReq);
            sendData.put("TermUrl", checkout.sendData.termUrl);
            htmlPageContent = call(checkout.url, sendData.toString(), "application/json", interceptor);
        } else {
            final String urlEncoded =
                    "MD=" + URLEncoder.encode(checkout.sendData.md, "UTF-8") + "&" +
                            "PaReq=" + URLEncoder.encode(checkout.sendData.paReq, "UTF-8") + "&" +
                            "TermUrl=" + URLEncoder.encode(checkout.sendData.termUrl, "UTF-8");

            htmlPageContent = call(checkout.url, urlEncoded, "application/x-www-form-urlencoded", interceptor);
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1 || TextUtils.isEmpty(contentType[0])) {
            contentType[0] = "text/html";
        }

        final CloudipspView.PayConfirmation confirmation = new CloudipspView.PayConfirmation(htmlPageContent, contentType[0], checkout.url, new CloudipspView.PayConfirmation.Listener() {
            @Override
            public void onConfirmed(final String jsonOfConfirmation) {
                new RunnableWithExceptionWrapper(callback) {
                    @Override
                    protected void runInTry() throws java.lang.Exception {
                        final JSONObject response = new JSONObject(jsonOfConfirmation);
                        if (!response.getString("url").equals(URL_CALLBACK)) {
                            throw new java.lang.Exception();
                        }
                        final JSONObject orderData = response.getJSONObject("params");
                        checkResponse(orderData);
                        callback.onPaidProcessed(parseOrder(orderData));
                    }
                }.run();
            }
        });

        sMain.post(new Runnable() {
            @Override
            public void run() {
                cloudipspView.confirm(confirmation);
            }
        });
    }

    private static String call(String url, String content, String contentType) throws java.lang.Exception {
        return call(url, content, contentType, null);
    }

    private interface ResponseInterceptor {
        public void onIntercept(HttpURLConnection httpURLConnection);
    }

    private static String call(String url, String content, String contentType, ResponseInterceptor responseInterceptor) throws java.lang.Exception {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        if (connection instanceof HttpsURLConnection) {
            final HttpsURLConnection secureConnection = (HttpsURLConnection) connection;
            if (tlsSocketFactory != null) {
                secureConnection.setSSLSocketFactory(tlsSocketFactory);
            }
            secureConnection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    final String peerHost = session.getPeerHost();
                    return hostname.equals(peerHost);
                }
            });
        }
        final byte[] sentBytes = content.getBytes();
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Content-Length", String.valueOf(sentBytes.length));
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            final OutputStream output = connection.getOutputStream();
            output.write(sentBytes);
            output.flush();
//            if (BuildConfig.DEBUG) {
//                Log.i("Cloudipsp", "Sent:" + content);
//            }
            connection.connect();
            final int contentLength = connection.getHeaderFieldInt("ContentLength", 350);
            if (responseInterceptor != null) {
                responseInterceptor.onIntercept(connection);
            }
            final StringBuilder sb = new StringBuilder(contentLength);
            readAll(connection.getInputStream(), sb);
            return sb.toString();
        } finally {
            connection.disconnect();
        }
    }

    private static void readAll(InputStream from, StringBuilder to) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(from, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            to.append(line);
            to.append('\n');
        }
    }

    public static class Exception extends java.lang.Exception {
        Exception(String detailMessage) {
            super(detailMessage);
        }

        Exception(Throwable throwable) {
            super(throwable);
        }

        public final static class Failure extends Exception {
            public final int errorCode;
            public final String requestId;

            Failure(String detailMessage, int errorCode, String requestId) {
                super(detailMessage);
                this.errorCode = errorCode;
                this.requestId = requestId;
            }
        }

        public final static class IllegalServerResponse extends Exception {
            IllegalServerResponse(Throwable throwable) {
                super(throwable);
            }
        }

        public final static class NetworkSecurity extends Exception {
            NetworkSecurity(String detailMessage) {
                super(detailMessage);
            }
        }

        public final static class NetworkAccess extends Exception {
            NetworkAccess(String detailMessage) {
                super(detailMessage);
            }
        }

        public final static class ServerInternalError extends Exception {
            ServerInternalError(Throwable throwable) {
                super(throwable);
            }
        }

        public final static class Unknown extends Exception {
            Unknown(Throwable throwable) {
                super(throwable);
            }
        }
    }
}
