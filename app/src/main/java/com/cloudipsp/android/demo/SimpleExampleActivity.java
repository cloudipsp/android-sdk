package com.cloudipsp.android.demo;

import android.os.Bundle;
import android.widget.EditText;

import com.cloudipsp.android.Card;
import com.cloudipsp.android.CardInputView;

public class SimpleExampleActivity extends BaseExampleActivity {
    private CardInputView cardInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cardInput = findViewById(R.id.card_input);
        if (BuildConfig.DEBUG) {
            cardInput.setHelpedNeeded(true);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_simple_example;
    }

    @Override
    protected Card getCard() {
        return cardInput.confirm(new CardInputView.ConfirmationErrorHandler() {
            @Override
            public void onCardInputErrorClear(CardInputView view, EditText editText) {

            }

            @Override
            public void onCardInputErrorCatched(CardInputView view, EditText editText, String error) {

            }
        });
    }
}