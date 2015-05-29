package com.okihita.glutracker.util;

import android.content.Context;
import android.preference.PreferenceManager;

public class Config {
    public static final String TAG = "RSY";

    public static final String LOGGED_IN_USER_NAME = "username";
    public static final String LOGGED_IN_USER_ID = "userid";

    public static final String BASE_URL = "192.168.137.1"; // localhost
    // public static final String BASE_URL = "okihita.com"; // localhost
    public static final String SUBDOMAIN_ADDRESS = "urs";

    public static final String USER_ENTRY_POINT = "u.php";
    public static final String SIGNUP_ENTRY_POINT = "signup.php";

    public static final String AGE = "age";
    public static final String AGE_RANGE = "agerange";
    private static final String IS_PREGNANT = "ispregnant";
    public static final String IS_DIABETES = "isdiabetes";

    public static final String MEASUREMENT_ADDITION_ENTRY_POINT = "add.php";

    public static final int MEASUREMENT_MODE_PREMEAL = 1;
    public static final int MEASUREMENT_MODE_POSTMEAL = 2;
    public static final int MEASUREMENT_MODE_RANDOM = 3;

    private static final int[] diabetes = {76, 126, 79, 200, 76, 200};
    private static final int[] nondiabetes = {75, 100, 75, 140, 75, 140};
    private static final int[] pregnant = {60, 92, 60, 153, 60, 153};

    public static final String[] commentLow = {"You need to eat.", "Take a rest and eat some food.", "Call your doctor!", "Go to hospital!", "You are in danger!"};
    public static final String[] commentNormal = {"Great!", "Excellent!", "Keep it up!"};
    public static final String[] commentHigh = {"Do not eat too much!", "How about taking some exercise.", "Relax, don't be to stressed.", "Please take your insulin and pills!", "Call your doctor!"};

    // For manual input temporary storage.
    public static final String MANUAL_INPUT_YEAR = "com.okihita.manualyear";
    public static final String MANUAL_INPUT_MONTH = "com.okihita.manualmonth";
    public static final String MANUAL_INPUT_DAY = "com.okihita.manualday";
    public static final String MANUAL_INPUT_HOUR = "com.okihita.manualhour";
    public static final String MANUAL_INPUT_MINUTE = "com.okihita.manualminute";
    public static final String MANUAL_INPUT_TYPE = "com.okihita.manualtype";
    public static final String MANUAL_INPUT_VALUE = "com.okihita.manualvalue";


    /**
     * 1 = LOW, 2 = NORMAL, 3 = HIGH
     **/
    public static int bloodSugarLevel(Context context, int measurementMode, int value) {
        boolean isDiabetes = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Config.IS_DIABETES, false);
        boolean isPregnant = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Config.IS_PREGNANT, false);

        int[] category;
        int sugarlevel = 1; // by default, sugar level will return 'Low'

        /* Selecting category. */
        if (isPregnant)
            category = pregnant;
        else if (isDiabetes)
            category = diabetes;
        else
            category = nondiabetes;

        assert category != null;
        if (value > category[(2 * (measurementMode - 1))]) // sugar level normal
            sugarlevel = 2;
        if (value > category[(2 * (measurementMode - 1)) + 1]) // sugar level high
            sugarlevel = 3;

        return sugarlevel; // 1 = low, 2 = normal, 3 = high
    }
}
