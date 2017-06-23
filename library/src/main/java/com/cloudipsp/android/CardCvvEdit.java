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
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        setInputType(EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD);
        setSingleLine();
    }
}
