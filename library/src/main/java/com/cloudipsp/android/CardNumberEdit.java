package com.cloudipsp.android;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by vberegovoy on 22.12.15.
 */
public final class CardNumberEdit extends EditText {

    public CardNumberEdit(Context context) {
        super(context);
    }

    public CardNumberEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardNumberEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    @Deprecated
    public Editable getText() {
        if (isParentCall()) {
            return super.getText();
        } else {
            throw new RuntimeException("unsupported operation");
        }
    }

    @Override
    @Deprecated
    public void setText(CharSequence text, BufferType type) {
        if (isParentCall()) {
            super.setText(text, type);
        } else {
            throw new RuntimeException("unsupported operation");
        }
    }

    private final boolean isParentCall() {
        final StackTraceElement stack[] = new Throwable().getStackTrace();
        return isParentClass(stack[2].getClassName()) || isParentClass(stack[3].getClassName());
    }

    private final boolean isParentClass(String className) {
        return className.equals(TextView.class.getName())
                || className.equals(EditText.class.getName())
                || className.equals("android.widget.Editor")
                || className.startsWith("android.widget.Editor$");
    }

    String getCardNumber() {
        return super.getText().toString();
    }

    void setCardNumber(String cardNumber) {
        super.setText(cardNumber, BufferType.NORMAL);
    }
}
