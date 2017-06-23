package com.cloudipsp.android;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by vberegovoy on 6/20/17.
 */

public final class CardInputLayout extends FrameLayout {
    private CardNumberEdit editCardNumber;
    private CardExpMmEdit editMm;
    private CardExpYyEdit editYy;
    CardCvvEdit editCvv;

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
        editCardNumber = findOne(CardNumberEdit.class);
        editMm = findOne(CardExpMmEdit.class);
        editYy = findOne(CardExpYyEdit.class);
        editCvv = findOne(CardCvvEdit.class);
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

    void setHelpCard(String card, String expMm, String expYy, String cvv) {
        editCardNumber.setTextInternal(card);
        editMm.setTextInternal(expMm);
        editYy.setTextInternal(expYy);
        editCvv.setTextInternal(cvv);
    }

    public Card confirm(ConfirmationErrorHandler handler) {
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

    private void setReadIds() {
        if (editCardNumber == null) {
            return;
        }
        editCardNumber.setId(R.id.edit_card_number);
        editMm.setId(R.id.edit_mm);
        editYy.setId(R.id.edit_yy);
        editCvv.setId(R.id.edit_cvv);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
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
