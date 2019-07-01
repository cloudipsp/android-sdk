package com.cloudipsp.android.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cloudipsp.android.Cloudipsp;
import com.cloudipsp.android.Currency;
import com.cloudipsp.android.Order;
import com.cloudipsp.android.Receipt;

/**
 * Created by vberegovoy on 6/20/17.
 */

public class MainActivity extends Activity implements Cloudipsp.PayCallback, Cloudipsp.GooglePayCallback {
    private static final int RC_GOOGLE_PAY = 100500;

    private Cloudipsp api;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = new Cloudipsp(900234, null);
    }

    public void onSimpleExampleClicked(View view) {
        startActivity(new Intent(this, SimpleExampleActivity.class));
    }

    public void onFlexibleExampleClicked(View view) {
        startActivity(new Intent(this, FlexibleExampleActivity.class));
    }

    @Override
    public void onGooglePayInitialized() {
        Log.i("MainActivity", "onGooglePayInitialized");
    }

    @Override
    public void onPaidProcessed(Receipt receipt) {

    }

    @Override
    public void onPaidFailure(Cloudipsp.Exception e) {
        Log.i("MainActivity", "onPaidFailure", e);
    }

    public void onGooglePayClicked(View view) {
        if (!Cloudipsp.supportsGooglePay(this)) {
            Toast.makeText(this, "GooglePay is not supported", Toast.LENGTH_LONG).show();
            return;
        }

        order = new Order(1, Currency.RUB, "vb_"+System.currentTimeMillis(), "Test GooglePay", "valik.beregovoy@gmail.com");
        api.googlePayInitialize(order, this, RC_GOOGLE_PAY, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_PAY) {
            if (!api.googlePayComplete(resultCode, data, order, this)) {
                Toast.makeText(this, "GooglePay has been canceled", Toast.LENGTH_LONG).show();
            }
        }
    }
}
