package com.cloudipsp.android.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cloudipsp.android.Card;
import com.cloudipsp.android.CardInputLayout;
import com.cloudipsp.android.Cloudipsp;
import com.cloudipsp.android.CloudipspWebView;
import com.cloudipsp.android.Currency;
import com.cloudipsp.android.Order;
import com.cloudipsp.android.Receipt;

/**
 * Created by vberegovoy on 6/20/17.
 */

public class FlexibleExampleActivity extends Activity implements View.OnClickListener {
    private static final int MERCHANT_ID = 1396424;

    private EditText editAmount;
    private Spinner spinnerCcy;
    private EditText editEmail;
    private EditText editDescription;
    private CardInputLayout cardLayout;
    private CloudipspWebView webView;

    private Cloudipsp cloudipsp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flexible_example);

        findViewById(R.id.btn_amount).setOnClickListener(this);
        editAmount = findViewById(R.id.edit_amount);
        spinnerCcy = findViewById(R.id.spinner_ccy);
        editEmail = findViewById(R.id.edit_email);
        editDescription = findViewById(R.id.edit_description);
        cardLayout = findViewById(R.id.card_layout);
        findViewById(R.id.btn_pay).setOnClickListener(this);

        webView = findViewById(R.id.web_view);
        cloudipsp = new Cloudipsp(MERCHANT_ID, webView);

        spinnerCcy.setAdapter(new ArrayAdapter<Currency>(this, android.R.layout.simple_spinner_item, Currency.values()));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_amount:
                fillTest();
                break;
            case R.id.btn_pay:
                processPay();
                break;
        }
    }

    private void fillTest() {
        editAmount.setText("1");
        editEmail.setText("test@test.com");
        editDescription.setText("test payment");
    }

    private void processPay() {
        editAmount.setError(null);
        editEmail.setError(null);
        editDescription.setError(null);

        final int amount;
        try {
            amount = Integer.valueOf(editAmount.getText().toString());
        } catch (Exception e) {
            editAmount.setError(getString(R.string.e_invalid_amount));
            return;
        }

        final String email = editEmail.getText().toString();
        final String description = editDescription.getText().toString();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError(getString(R.string.e_invalid_email));
        } else if (TextUtils.isEmpty(description)) {
            editDescription.setError(getString(R.string.e_invalid_description));
        } else {
            final Card card = cardLayout.confirm(new CardInputLayout.ConfirmationErrorHandler() {
                @Override
                public void onCardInputErrorClear(CardInputLayout view, EditText editText) {
                }

                @Override
                public void onCardInputErrorCatched(CardInputLayout view, EditText editText, String error) {
                    editText.setError(error);
                }
            });

            if (card != null) {
                final Currency currency = (Currency) spinnerCcy.getSelectedItem();
                final Order order = new Order(amount, currency, "vb_" + System.currentTimeMillis(), description, email);
                order.setLang(Order.Lang.ru);

                cloudipsp.pay(card, order, new Cloudipsp.PayCallback() {
                    @Override
                    public void onPaidProcessed(Receipt receipt) {
                        Toast.makeText(FlexibleExampleActivity.this, "Paid " + receipt.status.name() + "\nPaymentId:" + receipt.paymentId, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPaidFailure(Cloudipsp.Exception e) {
                        if (e instanceof Cloudipsp.Exception.Failure) {
                            Cloudipsp.Exception.Failure f = (Cloudipsp.Exception.Failure) e;

                            Toast.makeText(FlexibleExampleActivity.this, "Failure\nErrorCode: " +
                                    f.errorCode + "\nMessage: " + f.getMessage() + "\nRequestId: " + f.requestId, Toast.LENGTH_LONG).show();
                        } else if (e instanceof Cloudipsp.Exception.NetworkSecurity) {
                            Toast.makeText(FlexibleExampleActivity.this, "Network security error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else if (e instanceof Cloudipsp.Exception.ServerInternalError) {
                            Toast.makeText(FlexibleExampleActivity.this, "Internal server error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else if (e instanceof Cloudipsp.Exception.NetworkAccess) {
                            Toast.makeText(FlexibleExampleActivity.this, "Network error", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(FlexibleExampleActivity.this, "Payment Failed", Toast.LENGTH_LONG).show();
                        }
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.waitingForConfirm()) {
            webView.skipConfirm();
        } else {
            super.onBackPressed();
        }
    }
}
