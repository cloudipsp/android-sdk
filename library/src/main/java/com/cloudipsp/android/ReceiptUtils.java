package com.cloudipsp.android;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by vberegovoy on 11.07.16.
 */
public class ReceiptUtils {
    public static Map<String, Object> dumpFields(Receipt receipt) {
        final Iterator<String> keys = receipt.orderData.keys();
        final Map<String, Object> map = new HashMap<String, Object>();

        while (keys.hasNext()) {
            final String key = keys.next();
            final Object value = receipt.orderData.opt(key);

            if (value instanceof Boolean || value instanceof Number || value instanceof String) {
                map.put(key, value);
            }
        }

        return map;
    }
}
