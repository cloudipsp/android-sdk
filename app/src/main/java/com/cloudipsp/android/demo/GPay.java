package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudipsp.android.Cloudipsp;
import com.cloudipsp.android.CloudipspWebView;
import com.cloudipsp.android.GooglePayCall;
import com.cloudipsp.android.Order;
import com.cloudipsp.android.Receipt;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, // Implementing OnClickListener for handling button clicks
        Cloudipsp.PayCallback, // Implementing Cloudipsp.PayCallback for payment callbacks
        Cloudipsp.GooglePayCallback { // Implementing Cloudipsp.GooglePayCallback for Google Pay callbacks

    private static final int RC_GOOGLE_PAY = 100500;
    private static final String K_GOOGLE_PAY_CALL = "google_pay_call";
    private Cloudipsp cloudipsp;
    private GooglePayCall googlePayCall; // <- this should be serialized on saving instance state
    private CloudipspWebView webView;
    private Button googlePayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for this activity

        // Initialize UI elements
        webView = findViewById(R.id.webView); // Initialize CloudipspWebView from layout
        googlePayButton = findViewById(R.id.google_pay_button); // Initialize Button from layout
        googlePayButton.setOnClickListener(this); // Set click listener for Google Pay button

        // Check if Google Pay is supported and set button visibility accordingly
        if (Cloudipsp.supportsGooglePay(this)) {
            googlePayButton.setVisibility(View.VISIBLE); // Show Google Pay button
        } else {
            googlePayButton.setVisibility(View.GONE); // Hide Google Pay button if unsupported
            Toast.makeText(this, R.string.e_google_pay_unsupported, Toast.LENGTH_LONG).show(); // Show unsupported message
        }

        if (savedInstanceState != null) {
            googlePayCall = savedInstanceState.getParcelable(K_GOOGLE_PAY_CALL);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.waitingForConfirm()) {
            webView.skipConfirm(); // Skip confirmation in WebView if waiting
        } else {
            super.onBackPressed(); // Otherwise, perform default back button behavior
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.google_pay_button) {
            processGooglePay(); // Handle click on Google Pay button
//            processGooglePayWithToken(); // Handle click on Google Pay button
        }
    }

    private void processGooglePay() {
        // Initialize Cloudipsp with merchant ID and WebView
        cloudipsp = new Cloudipsp(0, webView); // Initialize the payment process with the merchant ID
        final Order googlePayOrder = createOrder(); // Create order for Google Pay payment
        if (googlePayOrder != null) {
            cloudipsp.googlePayInitialize(googlePayOrder, this, RC_GOOGLE_PAY, this); // Initialize Google Pay payment
        }
    }

    private void processGooglePayWithToken() {
        // Initialize Cloudipsp with merchant ID and WebView
        cloudipsp = new Cloudipsp(0, webView); // Initialize the payment process with the merchant ID
        cloudipsp.googlePayInitialize("321d7ebe83c2b34ce38fee59c4c845e9fef67a0b", this, RC_GOOGLE_PAY, this); // Initialize Google Pay payment
    }



    private Order createOrder() {
        final int amount = 100;
        final String email = "test@gmail.com";
        final String description = "test payment";
        final String currency = "GEL";
        return new Order(amount, currency, "vb_" + System.currentTimeMillis(), description, email); // Create and return new payment order
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(K_GOOGLE_PAY_CALL, googlePayCall);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_GOOGLE_PAY:
                if (!cloudipsp.googlePayComplete(resultCode, data, googlePayCall, this)) {
                    Toast.makeText(this, R.string.e_google_pay_canceled, Toast.LENGTH_LONG).show(); // Show payment canceled message
                }
                break;
        }
    }

    @Override
    public void onPaidProcessed(Receipt receipt) {
        Toast.makeText(this, "Paid " + receipt.status.name() + "\nPaymentId:" + receipt.paymentId, Toast.LENGTH_LONG).show(); // Show payment success message
        Log.d("PaymentStatus", "Paid " + receipt.status.name() + " PaymentId: " + receipt.paymentId);

    }

    @Override
    public void onPaidFailure(Cloudipsp.Exception e) {
        if (e instanceof Cloudipsp.Exception.Failure) {
            Cloudipsp.Exception.Failure f = (Cloudipsp.Exception.Failure) e;
            Toast.makeText(this, "Failure\nErrorCode: " +
                    f.errorCode + "\nMessage: " + f.getMessage() + "\nRequestId: " + f.requestId, Toast.LENGTH_LONG).show(); // Show specific failure details
        } else if (e instanceof Cloudipsp.Exception.NetworkSecurity) {
            Toast.makeText(this, "Network security error: " + e.getMessage(), Toast.LENGTH_LONG).show(); // Show network security error
        } else if (e instanceof Cloudipsp.Exception.ServerInternalError) {
            Toast.makeText(this, "Internal server error: " + e.getMessage(), Toast.LENGTH_LONG).show(); // Show internal server error
        } else if (e instanceof Cloudipsp.Exception.NetworkAccess) {
            Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show(); // Show network access error
        } else {
            Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show(); // Show generic payment failure
        }
        e.printStackTrace(); // Print stack trace for debugging
    }

    @Override
    public void onGooglePayInitialized(GooglePayCall result) {
        // Handle Google Pay initialization if needed
        Toast.makeText(this, "Google Pay initialized", Toast.LENGTH_LONG).show(); // Show Google Pay initialization message
        this.googlePayCall = result; // Store Google Pay call result
    }
}
