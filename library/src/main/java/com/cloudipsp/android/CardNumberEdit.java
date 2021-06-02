package com.cloudipsp.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;

/**
 * Created by vberegovoy on 22.12.15.
 */
public final class CardNumberEdit extends CardInputBase {
    private static final int MIN_NUMBER_LENGTH = 16;
    private static final int MAX_NUMBER_LENGTH = 19;
    private CreditCardFormatTextWatcher formatTextWatcher;

    public CardNumberEdit(Context context) {
        super(context);
        init();
    }

    public CardNumberEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardNumberEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setRawInputType(EditorInfo.TYPE_CLASS_NUMBER);
        setCardNumberFormatting(true);
        setFiltersInternal(new InputFilter[]{new InputFilter.LengthFilter(MAX_NUMBER_LENGTH)});
        setSingleLine();
    }

    @Override
    protected CharSequence getMaskedValue() {
        final CharSequence cardNumber = getTextInternal();
        final int length = cardNumber.length();

        if (length >= MIN_NUMBER_LENGTH) {
            final StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < 6) {
                sb.append(cardNumber.charAt(i++));
            }
            final int tail = length - 4;
            while (i++ < tail) {
                sb.append('*');
            }
            while (i < length) {
                sb.append(cardNumber.charAt(i++));
            }
            return sb.toString();
        }
        return "";
    }

    public void setCardNumberFormatting(boolean enable) {
        if (enable) {
            if (formatTextWatcher == null) {
                formatTextWatcher = new CreditCardFormatTextWatcher(10, MAX_NUMBER_LENGTH);
            }
            removeTextChangedListener(formatTextWatcher);
            addTextChangedListenerInternal(formatTextWatcher);
        } else {
            if (formatTextWatcher != null) {
                removeTextChangedListener(formatTextWatcher);
            }
        }
    }

    private static class CreditCardFormatTextWatcher implements TextWatcher {
        private final int maxLength;
        private final int paddingPx;

        private boolean internalStopFormatFlag;

        private CreditCardFormatTextWatcher(int paddingPx, int maxLength) {
            this.paddingPx = paddingPx;
            this.maxLength = maxLength;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (internalStopFormatFlag) {
                return;
            }
            internalStopFormatFlag = true;
            formatCardNumber(s, paddingPx, maxLength);
            internalStopFormatFlag = false;
        }

        private static void formatCardNumber(@NonNull Editable ccNumber, int paddingPx, int maxLength) {
            final int textLength = ccNumber.length();
            final PaddingRightSpan[] spans = ccNumber.getSpans(0, ccNumber.length(), PaddingRightSpan.class);

            for (PaddingRightSpan span : spans) {
                ccNumber.removeSpan(span);
            }

            if (maxLength > 0 && textLength > maxLength - 1) {
                ccNumber.replace(maxLength, textLength, "");
            }

            for (int i = 1; i <= ((textLength - 1) / 4); i++) {
                final int end = i * 4;
                final int start = end - 1;
                final PaddingRightSpan marginSpan = new PaddingRightSpan(paddingPx);

                ccNumber.setSpan(marginSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        public static class PaddingRightSpan extends ReplacementSpan {
            private final int padding;

            private PaddingRightSpan(int padding) {
                this.padding = padding;
            }

            @Override
            public int getSize(@NonNull Paint paint, CharSequence text,
                               int start, int end, Paint.FontMetricsInt fm) {
                final float[] widths = new float[end - start];
                paint.getTextWidths(text, start, end, widths);

                int sum = padding;
                for (float width : widths) {
                    sum += width;
                }
                return sum;
            }

            @Override
            public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                             float x, int top, int y, int bottom, @NonNull Paint paint) {
                canvas.drawText(text, start, end, x, y, paint);
            }
        }
    }
}
