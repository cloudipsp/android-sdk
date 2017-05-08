package com.cloudipsp.android;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

/**
 * Created by vberegovoy on 22.12.15.
 */
public class CardInputView extends FrameLayout {
    private static final ConfirmationErrorHandler DEFAULT_CONFIRMATION_ERROR_HANDLER = new ConfirmationErrorHandler() {
        @Override
        public void onCardInputErrorClear(CardInputView view, EditText editText) {
            editText.setError(null);
        }

        @Override
        public void onCardInputErrorCatched(CardInputView view, EditText editText, String error) {
            editText.setError(error);
            editText.requestFocus();
        }
    };
    private static final String[] HELP_CARDS = new String[]{"4444555566661111", "4444111166665555", "4444555511116666", "4444111155556666"};

    private final View view;
    private final CardNumberEdit editCardNumber;
    private final EditText editMm;
    private final EditText editYy;
    private final EditText editCvv;

    private int currentHelpCard = 0;
    private boolean helpedNeeded = false;

    public CardInputView(Context context) {
        this(context, null);
    }

    public CardInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        view = LayoutInflater.from(context).inflate(R.layout.com_cloudipsp_android_card_input_view, null);
        editCardNumber = (CardNumberEdit) view.findViewById(R.id.edit_card_number);
        editMm = (EditText) view.findViewById(R.id.edit_mm);
        editYy = (EditText) view.findViewById(R.id.edit_yy);
        editCvv = (EditText) view.findViewById(R.id.edit_cvv);

        view.findViewById(R.id.btn_help_next_card).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nextHelpCard();
            }
        });

        addView(view);
        setFakeIds();
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        setReadIds();
        super.dispatchSaveInstanceState(container);
        setFakeIds();
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        setReadIds();
        super.dispatchRestoreInstanceState(container);
        setFakeIds();
    }

    private void setFakeIds() {
        editCardNumber.setId(View.NO_ID);
        editMm.setId(View.NO_ID);
        editYy.setId(View.NO_ID);
        editCvv.setId(View.NO_ID);
    }

    private void setReadIds() {
        editCardNumber.setId(R.id.edit_card_number);
        editMm.setId(R.id.edit_mm);
        editYy.setId(R.id.edit_yy);
        editCvv.setId(R.id.edit_cvv);
    }

    private void nextHelpCard() {
        if (helpedNeeded) {
            currentHelpCard %= HELP_CARDS.length;
            editCardNumber.setCardNumber(HELP_CARDS[currentHelpCard++]);
            editMm.setText("12");
            editYy.setText("18");
            editCvv.setText("111");
        }
    }

    public void setHelpedNeeded(boolean value) {
        helpedNeeded = value;
    }

    public boolean isHelpedNeeded() {
        return helpedNeeded;
    }

    public Card confirm() {
        return confirm(DEFAULT_CONFIRMATION_ERROR_HANDLER);
    }

    public Card confirm(ConfirmationErrorHandler handler) {
        handler.onCardInputErrorClear(this, editCardNumber);
        handler.onCardInputErrorClear(this, editMm);
        handler.onCardInputErrorClear(this, editYy);
        handler.onCardInputErrorClear(this, editCvv);

        final Card card = new Card
                (
                        editCardNumber.getCardNumber(),
                        editMm.getText().toString(),
                        editYy.getText().toString(),
                        editCvv.getText().toString()
                );

        if (!card.isValidCardNumber()) {
            handler.onCardInputErrorCatched(this, editCardNumber, getContext().getString(R.string.e_invalid_card_number));
        } else if (!card.isValidExpireMonth()) {
            handler.onCardInputErrorCatched(this, editMm, getContext().getString(R.string.e_invalid_mm));
        } else if (!card.isValidExpireYear()) {
            handler.onCardInputErrorCatched(this, editYy, getContext().getString(R.string.e_invalid_yy));
        } else if (!card.isValidExpireDate()) {
            handler.onCardInputErrorCatched(this, editYy, getContext().getString(R.string.e_invalid_date));
        } else if (!card.isValidCvv()) {
            handler.onCardInputErrorCatched(this, editCvv, getContext().getString(R.string.e_invalid_cvv));
        } else {
            return card;
        }
        return null;
    }

    public interface ConfirmationErrorHandler {
        public void onCardInputErrorClear(CardInputView view, EditText editText);

        public void onCardInputErrorCatched(CardInputView view, EditText editText, String error);
    }
}
