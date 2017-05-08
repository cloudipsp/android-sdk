package com.cloudipsp.android;

/**
 * Created by vberegovoy on 09.11.15.
 */
public enum Currency {
    UAH,
    RUB,
    USD,
    EUR,
    GBP;

    @Override
    public String toString() {
        return name();
    }


}
