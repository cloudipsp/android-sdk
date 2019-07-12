package com.cloudipsp.android;

import android.widget.EditText;
import android.widget.FrameLayout;

/**
 * Created by vberegovoy on 6/20/17.
 */
interface BaseConfirmationErrorHandler<V extends FrameLayout> {
    void onCardInputErrorClear(V view, EditText editText);

    void onCardInputErrorCatched(V view, EditText editText, String error);
}
