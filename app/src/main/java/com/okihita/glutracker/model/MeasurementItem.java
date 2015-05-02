package com.okihita.glutracker.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MeasurementItem {

    private static final String JSON_ID = "id";
    private static final String JSON_DATE = "date";
    private static final String JSON_KADAR = "kadar";
    private static final String JSON_JENIS = "jenis";

    private int mId;
    private int mKadar;
    private Date mTanggalAmbil;
    private int mJenis;

    public MeasurementItem() {
        mId = 0;
        mKadar = new Random().nextInt(40) + 80;
        mTanggalAmbil = new Date();
        mJenis = 0;
    }

    public MeasurementItem(JSONObject jsonObject) throws JSONException {
        mId = jsonObject.getInt(JSON_ID);
        mKadar = jsonObject.getInt(JSON_KADAR);
        mJenis = jsonObject.getInt(JSON_JENIS);
        try {
            mTanggalAmbil = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                    .parse(jsonObject.getString(JSON_DATE));
        } catch (ParseException ignored) {
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, mId);
        json.put(JSON_DATE, mTanggalAmbil.toString());
        json.put(JSON_KADAR, mKadar);
        json.put(JSON_JENIS, String.valueOf(mJenis));
        return json;
    }

    public int getId() {
        return mId;
    }

    public int getKadar() {
        return mKadar;
    }

    public Date getTanggalAmbil() {
        return mTanggalAmbil;
    }

    public int getJenis() {
        return mJenis;
    }

    public String getJenisTeks() {
        switch (mJenis) {
            case 1:
                return "PREMEAL";
            case 2:
                return "POSTMEAL";
            case 3:
                return "RANDOM";
        }
        return "INVALID";
    }
}
