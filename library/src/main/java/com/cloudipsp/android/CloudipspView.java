package com.cloudipsp.android;

import org.json.JSONObject;

/**
 * Created by vberegovoy on 28.11.15.
 */
public interface CloudipspView {
    void confirm(PayConfirmation confirmation);

    final class PayConfirmation {
        final String htmlPageContent;
        final String contentType;
        final String url;
        final String callbackUrl;
        final String host;
        final String cookie;
        final Listener listener;

        PayConfirmation(String htmlPageContent, String contentType, String url, String callbackUrl, String host, String cookie, Listener listener) {
            this.htmlPageContent = htmlPageContent;
            this.contentType = contentType;
            this.url = url;
            this.callbackUrl = callbackUrl;
            this.host = host;
            this.cookie = cookie;
            this.listener = listener;
        }

        interface Listener {
            void onConfirmed(JSONObject response);

            void onNetworkAccessError(String description);

            void onNetworkSecurityError(String description);
        }
    }

}
