package com.cloudipsp.android;

/**
 * Created by vberegovoy on 28.11.15.
 */
public interface CloudipspView {
    void confirm(PayConfirmation confirmation);

    public static final class PayConfirmation {
        final String htmlPageContent;
        final String contentType;
        final String url;
        final Listener listener;

        PayConfirmation(String htmlPageContent, String contentType, String url, Listener listener) {
            this.htmlPageContent = htmlPageContent;
            this.contentType = contentType;
            this.url = url;
            this.listener = listener;
        }

        interface Listener {
            public void onConfirmed(String jsonOfConfirmation);
        }
    }

}
