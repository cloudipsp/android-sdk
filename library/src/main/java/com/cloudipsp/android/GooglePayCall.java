package com.cloudipsp.android;

import android.os.Parcel;
import android.os.Parcelable;

public class GooglePayCall implements Parcelable {
    public static final Creator<GooglePayCall> CREATOR = new Creator<GooglePayCall>() {
        @Override
        public GooglePayCall createFromParcel(Parcel input) {
            return new GooglePayCall(input);
        }

        @Override
        public GooglePayCall[] newArray(int size) {
            return new GooglePayCall[size];
        }
    };

    final String token;
    final Order order;
    final String callbackUrl;
    final String paymentSystem;

    private GooglePayCall(Parcel input) {
        this.token = input.readString();
        this.order = input.readParcelable(Order.class.getClassLoader());
        this.callbackUrl = input.readString();
        this.paymentSystem = input.readString();
    }

    GooglePayCall(String token, Order order, String callbackUrl, String paymentSystem) {
        this.token = token;
        this.order = order;
        this.callbackUrl = callbackUrl;
        this.paymentSystem = paymentSystem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel output, int flags) {
        output.writeString(token);
        output.writeParcelable(order, 0);
        output.writeString(callbackUrl);
        output.writeString(paymentSystem);
    }
}
