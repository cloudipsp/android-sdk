package com.cloudipsp.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
* Created by vberegovoy on 28.11.15.
*/
public class CloudipspWebView extends WebView implements CloudipspView {
    private static final String URL_START_PATTERN = "http://secure-redirect.cloudipsp.com/submit/#";

    public CloudipspWebView(Context context) {
        super(context);
        init();
    }

    public CloudipspWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CloudipspWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        final WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setAppCacheEnabled(false);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowFileAccess(false);

        setVisibility(View.GONE);
    }

    public final boolean waitingForConfirm() {
        return getVisibility() == View.VISIBLE;
    }

    public final void skipConfirm() {
        stopLoading();
        setVisibility(View.GONE);
    }

    public final void confirm(final PayConfirmation confirmation) {
        if (confirmation == null) {
            throw new NullPointerException("confirmation should be not null");
        }
        setVisibility(View.VISIBLE);

        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                if (BuildConfig.DEBUG) {
//                    Log.i(Cloudipsp.TAG, "WebUrl: " + url);
//                }
                if (url.startsWith(URL_START_PATTERN)) {
                    final String jsonOfConfirmation = url.split(URL_START_PATTERN)[1];
                    confirmation.listener.onConfirmed(jsonOfConfirmation);

                    setVisibility(View.GONE);
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });


        loadDataWithBaseURL(confirmation.url, confirmation.htmlPageContent, confirmation.contentType, null, null);
    }
}
