package com.cloudipsp.android;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

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
        settings.setDatabaseEnabled(true);

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
                if (checkUrl(url)) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (checkUrl(request.getUrl().toString())) {
                        return true;
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                checkUrl(url);
            }

            private boolean checkUrl(String url) {
                if (BuildConfig.DEBUG) {
                    Log.i("Cloudipsp", "WebUrl: " + url);
                }
                boolean detectsStartPattern = url.startsWith(URL_START_PATTERN);
                boolean detectsCallbackUrl = false;
                boolean detectsApiToken = false;
                if (!detectsStartPattern) {
                    detectsCallbackUrl = url.startsWith(confirmation.callbackUrl);
                    if (!detectsCallbackUrl) {
                        detectsApiToken = url.startsWith(confirmation.host + "/api?token=");
                    }
                }

                if (detectsStartPattern || detectsCallbackUrl || detectsApiToken) {
                    blankPage();

                    JSONObject response = null;
                    if (detectsStartPattern) {
                        final String jsonOfConfirmation = url.split(URL_START_PATTERN)[1];
                        try {
                            response = new JSONObject(jsonOfConfirmation);
                        } catch (JSONException jsonException) {
                            try {
                                response = new JSONObject(URLDecoder.decode(jsonOfConfirmation, "UTF-8"));
                            } catch (Exception e) {
                                response = null;
                            }
                        }
                    }
                    confirmation.listener.onConfirmed(response);

                    setVisibility(View.GONE);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                handleError(errorCode, description);
            }

            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                handleError(error.getErrorCode(), error.getDescription().toString());
            }

            private void handleError(int errorCode, String description) {
                switch (errorCode) {
                    case WebViewClient.ERROR_HOST_LOOKUP:
                    case WebViewClient.ERROR_IO:
                    case WebViewClient.ERROR_CONNECT:
                        confirmation.listener.onNetworkAccessError(description);
                        handleError();
                        break;
                    case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
                        confirmation.listener.onNetworkSecurityError(description);
                        handleError();
                        break;
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                confirmation.listener.onNetworkSecurityError(error.toString());
                handleError();
            }

            private void handleError() {
                blankPage();
                skipConfirm();
            }
        });


        if (Tls12SocketFactory.needHere()) {
            loadProxy(confirmation);
        } else {
            final Runnable l = new Runnable() {
                @Override
                public void run() {
                    loadDataWithBaseURL(confirmation.url, confirmation.htmlPageContent, confirmation.contentType, encoding(confirmation.contentType), null);
                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && confirmation.cookie != null) {
                CookieManager.getInstance().setCookie(confirmation.url, confirmation.cookie, new ValueCallback<Boolean>() {
                    @Override
                    public void onReceiveValue(Boolean value) {
                        l.run();
                    }
                });
            } else {
                l.run();
            }
        }
    }

    private void blankPage() {
        loadDataWithBaseURL(null, "<html></html>", "text/html", "UTF-8", null);
        invalidate();
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
