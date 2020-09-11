package com.cloudipsp.android;

import android.content.Context;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by vberegovoy on 6/20/17.
 */

public final class CardInputLayout extends FrameLayout implements CardDisplay {
    private CardNumberEdit editCardNumber;
    private CardExpMmEdit editMm;
    private CardExpYyEdit editYy;
    CardCvvEdit editCvv;

    private Card displayedCard;

    public CardInputLayout(Context context) {
        super(context);
    }

    public CardInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        editCardNumber = findOne(CardNumberEdit.class);
        editMm = findOne(CardExpMmEdit.class);
        editYy = findOne(CardExpYyEdit.class);
        editCvv = findOne(CardCvvEdit.class);
        editCardNumber.addTextChangedListenerInternal(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                editCvv.setCvv4(CvvUtils.isCvv4Length(s.toString()));
            }
        });
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        setRealIds();
        super.dispatchSaveInstanceState(container);
        setFakeIds();
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        setRealIds();
        super.dispatchRestoreInstanceState(container);
        setFakeIds();
    }

    public void setCardNumberFormatting(boolean enable) {
        editCardNumber.setCardNumberFormatting(enable);
    }

    public void display(Card card) {
        boolean enabled = true;
        if (card == null) {
            editCardNumber.setTextInternal("");
            editMm.setTextInternal("");
            editYy.setTextInternal("");
            editCvv.setTextInternal("");
            displayedCard = null;
        } else if (card.source == Card.SOURCE_NFC) {
            enabled = false;
            editCardNumber.setTextInternal(formattedCardNumber(card.cardNumber));
            editMm.setTextInternal(String.valueOf(card.mm));
            editYy.setTextInternal(String.valueOf(card.yy));
            editCvv.setTextInternal("");
            displayedCard = card;
        }
        editCardNumber.setEnabled(enabled);
        editMm.setEnabled(enabled);
        editYy.setEnabled(enabled);
        editCvv.setEnabled(enabled);
    }

    static String formattedCardNumber(String cardNumber) {
        String masked = cardNumber.substring(0, 4) + " " + cardNumber.substring(4, 6);

        masked += "** **** ";
        masked += cardNumber.substring(12, 16);

        return masked;
    }

    void setHelpCard(String card, String expMm, String expYy, String cvv) {
        editCardNumber.setTextInternal(card);
        editMm.setTextInternal(expMm);
        editYy.setTextInternal(expYy);
        editCvv.setTextInternal(cvv);
        editCardNumber.setEnabled(true);
        editMm.setEnabled(true);
        editYy.setEnabled(true);
        editCvv.setEnabled(true);
    }

    public Card confirm(ConfirmationErrorHandler handler) {
        if (displayedCard != null) {
            return null;
        }

        handler.onCardInputErrorClear(this, editCardNumber);
        handler.onCardInputErrorClear(this, editMm);
        handler.onCardInputErrorClear(this, editYy);
        handler.onCardInputErrorClear(this, editCvv);

        final Card card = new Card
                (
                        editCardNumber.getTextInternal().toString(),
                        editMm.getTextInternal().toString(),
                        editYy.getTextInternal().toString(),
                        editCvv.getTextInternal().toString()
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

    private void setFakeIds() {
        if (editCardNumber == null) {
            return;
        }
        editCardNumber.setId(View.NO_ID);
        editMm.setId(View.NO_ID);
        editYy.setId(View.NO_ID);
        editCvv.setId(View.NO_ID);
    }

    private void setRealIds() {
        if (editCardNumber == null) {
            return;
        }
        editCardNumber.setId(R.id.edit_card_number);
        editMm.setId(R.id.edit_mm);
        editYy.setId(R.id.edit_yy);
        editCvv.setId(R.id.edit_cvv);
    }

    private <V extends CardInputBase> V findOne(Class<V> clazz) {
        final ArrayList<V> views = new ArrayList<V>();
        find(clazz, this, views);
        final int count = views.size();
        if (count == 0) {
            throw new RuntimeException(getClass().getName() + " should contains " + clazz.getName());
        }
        if (count > 1) {
            throw new RuntimeException(getClass().getName() + " should contains only one view " + clazz.getName()+". "+
                    "Now here "+count +" instances of "+clazz.getName()+".");
        }
        return views.get(0);
    }

    private static <V extends CardInputBase> void find(Class<V> clazz, ViewGroup parent, ArrayList<V> out) {
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            final View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                find(clazz, (ViewGroup) child, out);
            } else if (child.getClass().equals(clazz)) {
                out.add((V) child);
            }
        }
    }

    public interface ConfirmationErrorHandler extends BaseConfirmationErrorHandler<CardInputLayout> {
    }
}
