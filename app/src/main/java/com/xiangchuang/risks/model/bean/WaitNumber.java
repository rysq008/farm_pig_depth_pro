package com.xiangchuang.risks.model.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class WaitNumber implements Parcelable {
    private int number;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "WaitNumber{" +
                "number=" + number +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.number);
    }

    public WaitNumber() {
    }

    protected WaitNumber(Parcel in) {
        this.number = in.readInt();
    }

    public static final Parcelable.Creator<WaitNumber> CREATOR = new Parcelable.Creator<WaitNumber>() {
        @Override
        public WaitNumber createFromParcel(Parcel source) {
            return new WaitNumber(source);
        }

        @Override
        public WaitNumber[] newArray(int size) {
            return new WaitNumber[size];
        }
    };
}
