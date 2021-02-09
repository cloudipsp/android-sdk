package com.cloudipsp.android;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by vberegovoy on 09.11.15.
 */
public final class Card implements Parcelable {
    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel input) {
            return new Card(input);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    static final int INVALID_VALUE = -1;
    public static final int SOURCE_FORM = 0;
    public static final int SOURCE_NFC = 1;

    public enum Type {
        VISA {
            @Override
            protected boolean is(String cardNumber) {
                return cardNumber.charAt(0) == '4';
            }
        },
        MASTERCARD {
            @Override
            protected boolean is(String cardNumber) {
                if (cardNumber.charAt(0) != '5') {
                    return false;
                }
                final char a = cardNumber.charAt(1);
                return '0' <= a && a <= '5';
            }
        },
        MAESTRO {
            @Override
            protected boolean is(String cardNumber) {
                return cardNumber.charAt(0) == '6';
            }
        },
        UNKNOWN {
            @Override
            protected boolean is(String cardNumber) {
                return true;
            }
        };

        abstract protected boolean is(String cardNumber);

        private static Type fromCardNumber(String cardNumber) {
            for (Type type : values()) {
                if (type.is(cardNumber)) {
                    return type;
                }
            }
            return null;
        }
    }

    String cardNumber;
    int mm;
    int yy;
    String cvv;
    public final int source;

    Card(String cardNumber, String expireMm, String expireYy, String cvv) {
        this(cardNumber, expireMm, expireYy, cvv, SOURCE_FORM);
    }

    Card(String cardNumber, String expireMm, String expireYy, String cvv, int source) {
        setCardNumber(cardNumber);
        setExpireMonth(expireMm);
        setExpireYear(expireYy);
        setCvv(cvv);
        this.source = source;
    }

    private Card(Parcel input) {
        cardNumber = input.readString();
        mm = input.readInt();
        yy = input.readInt();
        cvv = input.readString();
        source = input.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel output, int flags) {
        output.writeString(cardNumber);
        output.writeInt(mm);
        output.writeInt(yy);
        output.writeString(cvv);
        output.writeInt(source);
    }

    public void setCvv(String value) {
        cvv = value;
    }

    public void setExpireMonth(String value) {
        try {
            mm = Integer.valueOf(value);
        } catch (Exception e) {
            mm = INVALID_VALUE;
        }
    }

    public void setExpireYear(String value) {
        try {
            yy = Integer.valueOf(value);
        } catch (Exception e) {
            yy = INVALID_VALUE;
        }
    }

    public void setCardNumber(String value) {
        cardNumber = value;
    }

    public boolean isValidExpireMonth() {
        return mm >= 1 && mm <= 12;
    }

    private boolean isValidExpireYearValue() {
        return yy >= 21 && yy <= 99;
    }

    public boolean isValidExpireYear() {
        if (!isValidExpireYearValue()) {
            return false;
        }
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR) - 2000;
        return year <= yy;
    }

    public boolean isValidExpireDate() {
        if (!isValidExpireMonth()) {
            return false;
        }
        if (!isValidExpireYear()) {
            return false;
        }

        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR) - 2000;

        return (yy > year) || (yy >= year && mm >= calendar.get(Calendar.MONTH) + 1);
    }

    public boolean isValidCvv() {
        if (source == SOURCE_FORM) {
            if (cvv == null) {
                return false;
            }
            final int length = cvv.length();
            if (CvvUtils.isCvv4Length(cardNumber)) {
                return length == 4;
            } else {
                return length == 3;
            }
        } else {
            return true;
        }
    }

    private static boolean lunaCheck(String cardNumber) {
        final char[] cardChars = cardNumber.toCharArray();

        int sum = 0;
        boolean odd = true;
        for (int i = cardChars.length - 1; i >= 0; --i) {
            final char a = cardChars[i];

            if (!('0' <= a && a <= '9')) {
                return false;
            }
            int num = (a - '0');
            odd = !odd;
            if (odd) {
                num *= 2;
            }
            if (num > 9) {
                num -= 9;
            }
            sum += num;
        }

        return sum % 10 == 0;
    }

    public boolean isValidCardNumber() {
        if (cardNumber == null) {
            return false;
        }

        final int length = cardNumber.length();
        if (!(12 <= length && length <= 19)) {
            return false;
        }

        if (!lunaCheck(cardNumber)) {
            return false;
        }

        return true;
    }

    public boolean isValidCard() {
        return isValidExpireDate() && isValidCvv() && isValidCardNumber();
    }

    public Type getType() {
        if (!isValidCardNumber()) {
            throw new IllegalStateException("CardNumber should be valid before for getType");
        }
        return Type.fromCardNumber(cardNumber);
    }
}
