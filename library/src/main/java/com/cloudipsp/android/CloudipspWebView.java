package com.cloudipsp.android;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
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
                    reload();

                    final String jsonOfConfirmation = url.split(URL_START_PATTERN)[1];
                    confirmation.listener.onConfirmed(jsonOfConfirmation);

                    setVisibility(View.GONE);
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }
        });


        if (Tls12SocketFactory.needHere()) {
            loadProxy(confirmation);
        } else {
            loadDataWithBaseURL(confirmation.url, confirmation.htmlPageContent, confirmation.contentType, encoding(confirmation.contentType), null);
        }
    }

    private static String encoding(String contentType) {
        String[] parts = contentType.split("charset\\=");
        if (parts.length < 2) {
            return "UTF-8";
        } else {
            return parts[1];
        }
    }

    private void loadProxy(PayConfirmation confirmation) {
        try {
            final Uri uri = Uri.parse(confirmation.url);
            final String oldHost = uri.getAuthority();
            final String newHost = "3dse.fondy.eu";

            final Uri.Builder uriBuilder = uri.buildUpon()
                    .authority(newHost)
                    .path(uri.getPath())
                    .appendQueryParameter("jd91mx8", oldHost);
            final String url = uriBuilder.toString();
            String htmlPageContent = confirmation.htmlPageContent;

            final String quoted = oldHost.replace(".", "\\.");
            htmlPageContent = htmlPageContent.replaceAll(quoted, newHost);


            clearProxy();
            loadDataWithBaseURL(url, htmlPageContent, confirmation.contentType, encoding(confirmation.contentType), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void clearProxy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getContext());
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }

        clearCache(true);
    }
}
