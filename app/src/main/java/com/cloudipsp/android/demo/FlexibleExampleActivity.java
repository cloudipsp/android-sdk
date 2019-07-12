package com.cloudipsp.android.demo;

import android.os.Bundle;
import android.widget.EditText;

import com.cloudipsp.android.Card;
import com.cloudipsp.android.CardInputLayout;

public class FlexibleExampleActivity extends BaseExampleActivity {
    private CardInputLayout cardLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cardLayout = findViewById(R.id.card_layout);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_flexible_example;
    }

    @Override
    protected Card getCard() {
        return cardLayout.confirm(new CardInputLayout.ConfirmationErrorHandler() {
            @Override
            public void onCardInputErrorClear(CardInputLayout view, EditText editText) {

            }

            @Override
            public void onCardInputErrorCatched(CardInputLayout view, EditText editText, String error) {

            }
        });
    }
}
