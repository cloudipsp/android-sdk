package com.cloudipsp.android;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

/**
 * Created by vberegovoy on 6/20/17.
 */

public class CardCvvEdit extends CardInputBase {
    public CardCvvEdit(Context context) {
        super(context);
        init();
    }

    public CardCvvEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardCvvEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setCvv4(false);
        setSingleLine();
        setInputType(EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD | EditorInfo.TYPE_CLASS_NUMBER);
    }

    void setCvv4(boolean enabled) {
        setFiltersInternal(new InputFilter[]{new InputFilter.LengthFilter(enabled ? 4 : 3)});
        if (!enabled) {
            final String cvv = getTextInternal().toString();
            if (cvv.length() == 4) {
                setTextInternal(cvv.substring(0, 3));
            }
        }
    }

    @Override
    protected CharSequence getMaskedValue() {
        final int length = getTextInternal().length();
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            builder.append('*');
        }
        return builder.toString();
    }
}
