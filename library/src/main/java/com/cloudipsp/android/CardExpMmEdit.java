package com.cloudipsp.android;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

/**
 * Created by vberegovoy on 6/20/17.
 */

public class CardExpMmEdit extends CardInputBase {
    public CardExpMmEdit(Context context) {
        super(context);
        init();
    }

    public CardExpMmEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardExpMmEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        setSingleLine();
    }
}
