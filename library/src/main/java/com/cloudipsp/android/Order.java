package com.cloudipsp.android;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Patterns;

import java.util.HashMap;

/**
 * Created by vberegovoy on 10.11.15.
 */
public class Order implements Parcelable {
    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel input) {
            return new Order(input);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    public enum Verification {
        amount,
        code
    }

    public enum Lang {
        ru,
        uk,
        en,
        lv,
        fr;
    }

    public final int amount;
    public final String currency;
    public final String id;
    public final String description;
    public final String email;

    String productId;
    String paymentSystems;
    String defaultPaymentSystem;
    int lifetime = -1;
    String merchantData;
    boolean preauth = false;
    boolean requiredRecToken = false;
    boolean verification = false;
    Verification verificationType = Verification.amount;
    String recToken;
    String version;
    Lang lang;
    String serverCallbackUrl;
    String reservationData;
    String paymentSystem;
    boolean delayed = false;

    final HashMap<String, String> arguments = new HashMap<String, String>();

    private Order(Parcel input) {
        amount = input.readInt();
        currency = input.readString();
        id = input.readString();
        description = input.readString();
        email = input.readString();
        productId = input.readString();
        paymentSystems = input.readString();
        defaultPaymentSystem = input.readString();
        lifetime = input.readInt();
        merchantData = input.readString();
        preauth = input.readInt() == 1;
        requiredRecToken = input.readInt() == 1;
        verification = input.readInt() == 1;
        verificationType = (Verification) input.readSerializable();
        recToken = input.readString();
        version = input.readString();
        lang = (Lang) input.readSerializable();
        serverCallbackUrl = input.readString();
        reservationData = input.readString();
        paymentSystem = input.readString();
        delayed = input.readInt() == 1;

        final Bundle bundle = input.readBundle();
        for (String key : bundle.keySet()) {
            arguments.put(key, bundle.getString(key));
        }
    }

    public Order(int amount, Currency currency, String id, String description) {
        this(amount, currency, id, description, null);
    }

    public Order(int amount, Currency currency, String id, String description, String email) {
        this(amount, currency == null ? null : currency.name(), id, description, email);
    }

    public Order(int amount, String currency, String id, String description, String email) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount should be more than 0");
        }
        if (currency == null) {
            throw new NullPointerException("currency should be not null");
        }
        if (id == null) {
            throw new NullPointerException("id should be not null");
        }
        if (id.length() == 0 || id.length() > 1024) {
            throw new IllegalArgumentException("id's length should be > 0 && <= 1024");
        }
        if (description == null) {
            throw new NullPointerException("description should be not null");
        }
        if (description.length() == 0 || description.length() > 1024) {
            throw new IllegalArgumentException("description's length should be > 0 && <= 1024");
        }
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw new IllegalArgumentException("email is not valid");
        }

        this.amount = amount;
        this.currency = currency;
        this.id = id;
        this.description = description;
        this.email = email;
    }

    public void setProductId(String value) {
        if (value == null) {
            throw new NullPointerException("ProductId should be not null");
        }
        if (value.length() > 1024) {
            throw new IllegalArgumentException("ProductId should be not more than 1024 symbols");
        }
        productId = value;
    }

    public void setPaymentSystems(String value) {
        if (value == null) {
            throw new NullPointerException("PaymentSystems should be not null");
        }
        paymentSystems = value;
    }

    public void setDelayed(boolean value) {
        delayed = value;
    }

    public void setDefaultPaymentSystem(String value) {
        if (value == null) {
            throw new NullPointerException("Default payment system should be not null");
        }
        defaultPaymentSystem = value;
    }

    public void setLifetime(int value) {
        lifetime = value;
    }

    public void setMerchantData(String value) {
        if (value == null) {
            throw new NullPointerException("MerchantData should be not null");
        }
        if (value.length() > 2048) {
            throw new IllegalArgumentException("MerchantData should be not more than 2048 symbols");
        }
        merchantData = value;
    }

    public void setPreauth(boolean enable) {
        preauth = enable;
    }

    public void setRequiredRecToken(boolean enable) {
        requiredRecToken = enable;
    }

    public void setVerification(boolean enable) {
        verification = enable;
    }

    public void setVerificationType(Verification type) {
        if (type == null) {
            throw new NullPointerException("VerificationType should be not null");
        }
        verificationType = type;
    }

    public void setRecToken(String value) {
        if (value == null) {
            throw new NullPointerException("RecToken should be not null");
        }
        recToken = value;
    }

    public void setVersion(String value) {
        if (value == null) {
            throw new NullPointerException("version should be not null");
        }
        if (value.length() > 10) {
            throw new IllegalArgumentException("version should be not more than 10 symbols");
        }
        version = value;
    }

    public void setLang(Lang value) {
        if (value == null) {
            throw new NullPointerException("Lang should be not null");
        }
        lang = value;
    }

    public void setServerCallbackUrl(String value) {
        if (value == null) {
            throw new NullPointerException("server callback url should be not null");
        }
        if (value.length() > 2048) {
            throw new IllegalArgumentException("server callback url should be not more than 10 symbols");
        }
        serverCallbackUrl = value;
    }

    public void setReservationData(String value) {
        if (value == null) {
            throw new NullPointerException("reservation data should be not null");
        }
        reservationData = value;
    }

    public void addArgument(String name, String value) {
        arguments.put(name, value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel output, int flags) {
        output.writeInt(amount);
        output.writeSerializable(currency);
        output.writeString(id);
        output.writeString(description);
        output.writeString(email);
        output.writeString(productId);
        output.writeString(paymentSystems);
        output.writeString(defaultPaymentSystem);
        output.writeInt(lifetime);
        output.writeString(merchantData);
        output.writeInt(preauth ? 1 : 0);
        output.writeInt(requiredRecToken ? 1 : 0);
        output.writeInt(verification ? 1 : 0);
        output.writeSerializable(verificationType);
        output.writeString(recToken);
        output.writeString(version);
        output.writeSerializable(lang);
        output.writeString(serverCallbackUrl);
        output.writeString(reservationData);
        output.writeString(paymentSystem);
        output.writeInt(delayed ? 1 : 0);

        final Bundle bundle = new Bundle();
        for (String key : arguments.keySet()) {
            bundle.putString(key, arguments.get(key));
        }
    }
}
