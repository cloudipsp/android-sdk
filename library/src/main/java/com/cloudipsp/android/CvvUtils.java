package com.cloudipsp.android;

/**
 * Created by vberegovoy on 2/19/18.
 */

class CvvUtils {
    private static final String[] CVV4_BINS = new String[] {"32", "33", "34", "37"};

    static boolean isCvv4Length(String cardNumber) {
        for (String bin : CVV4_BINS) {
            if (cardNumber.startsWith(bin)) {
                return true;
            }
        }
        return false;
    }
}
