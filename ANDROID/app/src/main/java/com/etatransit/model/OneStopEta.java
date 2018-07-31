package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by innovator on 8/27/2016.
 */
public class OneStopEta implements Parcelable {

    public int id;
    public List<OneStopEnRoute> enRoute = new ArrayList<OneStopEnRoute>();


    protected OneStopEta(Parcel in) {
        id = in.readInt();
        enRoute = in.createTypedArrayList(OneStopEnRoute.CREATOR);
    }

    public static final Creator<OneStopEta> CREATOR = new Creator<OneStopEta>() {
        @Override
        public OneStopEta createFromParcel(Parcel in) {
            return new OneStopEta(in);
        }

        @Override
        public OneStopEta[] newArray(int size) {
            return new OneStopEta[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeTypedList(enRoute);
    }
}
