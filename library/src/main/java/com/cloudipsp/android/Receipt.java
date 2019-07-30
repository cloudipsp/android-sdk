package com.cloudipsp.android;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by vberegovoy on 10.11.15.
 */
public class Receipt implements Parcelable {
    public static final Creator<Receipt> CREATOR = new Creator<Receipt>() {
        @Override
        public Receipt createFromParcel(Parcel input) {
            return new Receipt(input);
        }

        @Override
        public Receipt[] newArray(int size) {
            return new Receipt[size];
        }
    };

    public enum Status {
        created,
        processing,
        declined,
        approved,
        expired,
        reversed;
    }

    public enum TransationType {
        purchase,
        reverse,
        verification
    }

    public enum VerificationStatus {
        verified,
        incorrect,
        failed,
        created
    }

    public final String maskedCard;
    public final String cardBin;
    public final int amount;
    public final int paymentId;
    public final String currency;
    public final Status status;
    public final TransationType transationType;
    public final String senderCellPhone;
    public final String senderAccount;
    public final Card.Type cardType;
    public final String rrn;
    public final String approvalCode;
    public final String responseCode;
    public final String productId;
    public final String recToken;
    public final Date recTokenLifeTime;
    public final int reversalAmount;
    public final int settlementAmount;
    public final String settlementCurrency;
    public final Date settlementDate;
    public final int eci;
    public final int fee;
    public final int actualAmount;
    public final String actualCurrency;
    public final String paymentSystem;
    public final VerificationStatus verificationStatus;
    public final String signature;
    final String responseUrl;

    final JSONObject orderData;

    private Receipt(Parcel input) {
        maskedCard = input.readString();
        cardBin = input.readString();
        amount = input.readInt();
        paymentId = input.readInt();
        currency = input.readString();
        status = (Status) input.readSerializable();
        transationType = (TransationType) input.readSerializable();
        senderCellPhone = input.readString();
        senderAccount = input.readString();
        cardType = (Card.Type) input.readSerializable();
        rrn = input.readString();
        approvalCode = input.readString();
        responseCode = input.readString();
        productId = input.readString();
        recToken = input.readString();
        recTokenLifeTime = (Date)input.readSerializable();
        reversalAmount = input.readInt();
        settlementAmount = input.readInt();
        settlementCurrency = input.readString();
        settlementDate = (Date) input.readSerializable();
        eci = input.readInt();
        fee = input.readInt();
        actualAmount = input.readInt();
        actualCurrency = input.readString();
        paymentSystem = input.readString();
        verificationStatus = (VerificationStatus) input.readSerializable();
        signature = input.readString();
        responseUrl = input.readString();
        try {
            orderData = new JSONObject(input.readString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    Receipt(String maskedCard, String cardBin, int amount, int paymentId, String currency,
                    Status status, TransationType transationType, String senderCellPhone, String senderAccount,
                    Card.Type cardType, String rrn, String approvalCode, String responseCode, String productId,
                    String recToken, Date recTokenLifeTime, int reversalAmount, int settlementAmount,
                    String settlementCurrency, Date settlementDate, int eci, int fee, int actualAmount,
                    String actualCurrency, String paymentSystem, VerificationStatus verificationStatus,
                    String signature, String responseUrl, JSONObject orderData) {
        this.maskedCard = maskedCard;
        this.cardBin = cardBin;
        this.amount = amount;
        this.paymentId = paymentId;
        this.currency = currency;
        this.status = status;
        this.transationType = transationType;
        this.senderCellPhone = senderCellPhone;
        this.senderAccount = senderAccount;
        this.cardType = cardType;
        this.rrn = rrn;
        this.approvalCode = approvalCode;
        this.responseCode = responseCode;
        this.productId = productId;
        this.recToken = recToken;
        this.recTokenLifeTime = recTokenLifeTime;
        this.reversalAmount = reversalAmount;
        this.settlementAmount = settlementAmount;
        this.settlementCurrency = settlementCurrency;
        this.settlementDate = settlementDate;
        this.eci = eci;
        this.fee = fee;
        this.actualAmount = actualAmount;
        this.actualCurrency = actualCurrency;
        this.paymentSystem = paymentSystem;
        this.verificationStatus = verificationStatus;
        this.signature = signature;
        this.responseUrl = responseUrl;
        this.orderData = orderData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel output, int flags) {
        output.writeString(maskedCard);
        output.writeString(cardBin);
        output.writeInt(amount);
        output.writeInt(paymentId);
        output.writeSerializable(currency);
        output.writeSerializable(status);
        output.writeSerializable(transationType);
        output.writeString(senderCellPhone);
        output.writeString(senderAccount);
        output.writeSerializable(cardType);
        output.writeString(rrn);
        output.writeString(approvalCode);
        output.writeString(responseCode);
        output.writeString(productId);
        output.writeString(recToken);
        output.writeSerializable(recTokenLifeTime);
        output.writeInt(reversalAmount);
        output.writeInt(settlementAmount);
        output.writeSerializable(settlementCurrency);
        output.writeSerializable(settlementDate);
        output.writeInt(eci);
        output.writeInt(fee);
        output.writeInt(actualAmount);
        output.writeSerializable(actualCurrency);
        output.writeString(paymentSystem);
        output.writeSerializable(verificationStatus);
        output.writeString(signature);
        output.writeString(responseUrl);
        output.writeString(orderData.toString());
    }
}
