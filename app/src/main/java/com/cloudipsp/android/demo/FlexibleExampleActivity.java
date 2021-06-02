package com.cloudipsp.android.demo;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.cloudipsp.android.Card;
import com.cloudipsp.android.CardInputLayout;
import com.cloudipsp.android.Cloudipsp;

public class FlexibleExampleActivity extends BaseExampleActivity {
    private EditText editCard;
    private EditText editExpYy;
    private EditText editExpMm;
    private EditText editCvv;
    private CardInputLayout cardLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        editCard = findViewById(R.id.edit_card_number);
        editExpYy = findViewById(R.id.edit_yy);
        editExpMm = findViewById(R.id.edit_mm);
        editCvv = findViewById(R.id.edit_cvv);
        // ^^^ these fields used only as example for Cloudipsp.setStrictUiBlocking(false);
        cardLayout = findViewById(R.id.card_layout);
        cardLayout.setCardNumberFormatting(false);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_flexible_example;
    }

    @Override
    protected Card getCard() {
//        Cloudipsp.setStrictUiBlocking(false);
//        Log.i("Cloudipsp", "CardNumber: " + editCard.getText());
//        Log.i("Cloudipsp", "ExpYy: " + editExpYy.getText());
//        Log.i("Cloudipsp", "ExpMm: " + editExpMm.getText());
//        Log.i("Cloudipsp", "Cvv: " + editCvv.getText());

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
