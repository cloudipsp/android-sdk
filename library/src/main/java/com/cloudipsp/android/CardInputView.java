package com.cloudipsp.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by vberegovoy on 22.12.15.
 */
public final class CardInputView extends FrameLayout implements CardDisplay {
    private static final ConfirmationErrorHandler DEFAULT_CONFIRMATION_ERROR_HANDLER = new ConfirmationErrorHandler() {
        @Override
        public void onCardInputErrorClear(CardInputView view, EditText editText) {
            editText.setError(null);
        }

        @Override
        public void onCardInputErrorCatched(CardInputView view, EditText editText, String error) {
            editText.setError(error);
            editText.requestFocus();
        }
    };
    private static final String[] HELP_CARDS = new String[]{"4444555566661111", "4444111166665555", "4444555511116666", "4444111155556666"};

    private final CardInputLayout view;
    private CompletionListener completionListener;

    private int currentHelpCard = 0;
    private boolean helpedNeeded = false;

    public CardInputView(Context context) {
        this(context, null);
    }

    public CardInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        view = (CardInputLayout) LayoutInflater.from(context).inflate(R.layout.com_cloudipsp_android_card_input_view, null);
        view.findViewById(R.id.btn_help_next_card).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nextHelpCard();
            }
        });
        addView(view);
        setCompletionListener(null);
    }

    public void setCompletionListener(CompletionListener listener) {
        setCompletionListener(listener, EditorInfo.IME_ACTION_DONE);
    }

    public void setCompletionListener(final CompletionListener listener, final int lastViewImeOptions) {
        completionListener = listener;

        view.editCvv.setImeOptions(lastViewImeOptions);
        if (completionListener != null) {
            view.editCvv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (lastViewImeOptions == actionId) {
                        listener.onCompleted(CardInputView.this);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void nextHelpCard() {
        if (helpedNeeded) {
            currentHelpCard %= HELP_CARDS.length;
            view.setHelpCard(HELP_CARDS[currentHelpCard++], "12", "29", "111");
        }
    }

    public void setHelpedNeeded(boolean value) {
        helpedNeeded = value;
    }

    public void setCardNumberFormatting(boolean enable) {
        view.setCardNumberFormatting(enable);
    }

    public boolean isHelpedNeeded() {
        return helpedNeeded;
    }

    public void display(Card card) {
        view.display(card);
    }

    public Card confirm() {
        return confirm(DEFAULT_CONFIRMATION_ERROR_HANDLER);
    }

    public Card confirm(final ConfirmationErrorHandler handler) {
        return view.confirm(new CardInputLayout.ConfirmationErrorHandler() {
            @Override
            public void onCardInputErrorClear(CardInputLayout view, EditText editText) {
                handler.onCardInputErrorClear(CardInputView.this, editText);
            }

            @Override
            public void onCardInputErrorCatched(CardInputLayout view, EditText editText, String error) {
                handler.onCardInputErrorCatched(CardInputView.this, editText, error);
            }
        });
    }

    public interface ConfirmationErrorHandler extends BaseConfirmationErrorHandler<CardInputView> {
    }

    public interface CompletionListener {
        void onCompleted(CardInputView view);
    }
}
