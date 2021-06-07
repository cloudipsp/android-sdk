package com.cloudipsp.android;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by vberegovoy on 6/20/17.
 */

abstract class CardInputBase extends EditText {
    public CardInputBase(Context context) {
        super(context);
    }

    public CardInputBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardInputBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        if (isParentCall()) {
            super.addTextChangedListener(watcher);
        } else {
            if (Cloudipsp.strictUiBlocking) {
                throw new RuntimeException("unsupported operation");
            }
        }
    }

    @Override
    public void setFilters(InputFilter[] filters) {
        if (isParentCall()) {
            super.setFilters(filters);
        } else {
            if (Cloudipsp.strictUiBlocking) {
                throw new RuntimeException("unsupported operation");
            }
        }
    }

    protected void setFiltersInternal(InputFilter[] filters) {
        super.setFilters(filters);
    }

    void addTextChangedListenerInternal(TextWatcher watcher) {
        super.addTextChangedListener(watcher);
    }

    protected void setTextInternal(CharSequence text) {
        super.setText(text, BufferType.NORMAL);
    }

    protected CharSequence getTextInternal() {
        return super.getText();
    }

    @Override
    @Deprecated
    public Editable getText() {
        if (isParentCall()) {
            return super.getText();
        } else {
            if (Cloudipsp.strictUiBlocking) {
                throw new RuntimeException("unsupported operation");
            } else {
                return Editable.Factory.getInstance().newEditable(getMaskedValue());
            }
        }
    }

    protected abstract CharSequence getMaskedValue();

    @Override
    @Deprecated
    public void setText(CharSequence text, BufferType type) {
        if (isParentCall()) {
            super.setText(text, type);
        } else {
            if (Cloudipsp.strictUiBlocking) {
                throw new RuntimeException("unsupported operation");
            }
        }
    }

    private boolean isParentCall() {
        final StackTraceElement[] stack = new Throwable().getStackTrace();

        return isParentClass(stack[2].getClassName()) ||
                isParentClass(stack[3].getClassName()) ||
                isParentClass(stack[4].getClassName());
    }

    private boolean isParentClass(String className) {
        return className.startsWith("android.widget.")
                || className.startsWith("android.view.")
                || className.startsWith("com.google.android.material.textfield.TextInputLayout")
                || className.equals("android.support.design.widget.TextInputLayout")
                || className.equals("com.huawei.android.hwcontrol.HwEditor")
                || className.startsWith("com.letv.leui.text.");
    }
}
