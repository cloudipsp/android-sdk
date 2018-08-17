package com.cloudipsp.android;

/**
 * Created by vberegovoy on 09.11.15.
 */
public enum Currency {
    UAH,
    RUB,
    USD,
    EUR,
    GBP,
    KZT;

    @Override
    public String toString() {
        return name();
    }


}
