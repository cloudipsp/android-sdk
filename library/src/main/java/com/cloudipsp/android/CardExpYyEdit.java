package com.cloudipsp.android;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

/**
 * Created by vberegovoy on 6/20/17.
 */

public class CardExpYyEdit extends CardInputBase {
    public CardExpYyEdit(Context context) {
        super(context);
        init();
    }

    public CardExpYyEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardExpYyEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        setSingleLine();
    }
}
