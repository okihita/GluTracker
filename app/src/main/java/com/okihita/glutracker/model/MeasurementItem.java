package com.okihita.glutracker.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MeasurementItem {
    private static final String JSON_DATE = "date";
    private static final String JSON_KADAR = "kadar";
    private static final String JSON_JENIS = "jenis";

    private int mKadar;
    private Date mTanggalAmbil;
    private int mJenis;

    public MeasurementItem(JSONObject jsonObject) throws JSONException {
        mKadar = jsonObject.getInt(JSON_KADAR);
        mJenis = jsonObject.getInt(JSON_JENIS);
        try {
            mTanggalAmbil = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("id", "ID"))).parse(jsonObject.getString(JSON_DATE));
        } catch (ParseException ignored) {
        }
    }

    public int getKadar() {
        return mKadar;
    }

    public Date getTanggalAmbil() {
        return mTanggalAmbil;
    }

    /**
     * 1: PREMEAL<br/>
     * 2: POSTMEAL<br/>
     * 3: RANDOM<br/>
     */
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
