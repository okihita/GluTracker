package com.okihita.glutracker.util;

import android.content.Context;
import android.preference.PreferenceManager;

public class Config {
    public static final String LOGGED_IN_USER_NAME = "username";
    public static final String TAG = "RSY";

    public static final String BASE_URL = "192.168.137.1"; // localhost
    // public static final String BASE_URL = "okihita.com"; // localhost
    public static final String SUBDOMAIN_ADDRESS = "urs";

    public static final String LOGGED_IN_USER_ID = "userid";
    public static final String USER_ENTRY_POINT = "u.php";
    public static final String SIGNUP_ENTRY_POINT = "signup.php";

    public static final String AGE = "age";
    public static final String AGE_RANGE = "agerange";
    public static final String IS_PREGNANT = "ispregnant";
    public static final String IS_DIABETES = "isdiabetes";

    public static final String MEASUREMENT_ADDITION_ENTRY_POINT = "add.php";
    public static final int MEASUREMENT_MODE_PREMEAL = 1;
    public static final int MEASUREMENT_MODE_POSTMEAL = 2;
    public static final int MEASUREMENT_MODE_RANDOM = 3;

    /**
     * isPregnant:
     * -- TRUE or FALSE
     * ageRange:
     * -- 1 = less than 6 years old
     * -- 2 = 6 to 12 years old
     * -- 3 = 13 to 19 years old
     * -- 4 = more than 19 years old
     * isDiabetes:
     * -- TRUE or FALSE
     * mode:
     * -- 1 = premeal
     * -- 2 = postmeal
     * -- 3 = random
     * concentrationRange:
     * -- 1 = low
     * -- 2 = normal
     * -- 3 = high
     */

    private static final int[] pregnantWith = {50, 59, 60, 99, 100, 130, 80, 99, 100, 129, 130, 180, 50, 79, 80, 120, 121, 160};
    private static final int[] pregnantWithout = {50, 59, 60, 95, 96, 126, 80, 119, 120, 140, 141, 200, 50, 79, 80, 120, 121, 180};
    private static final int[] less6With = {60, 99, 100, 180, 181, 200, 80, 109, 110, 200, 201, 250, 60, 99, 100, 180, 291, 220};
    private static final int[] less6Without = {60, 69, 70, 100, 101, 130, 80, 99, 100, 170, 171, 220, 60, 79, 80, 150, 151, 200};
    private static final int[] from6to12With = {60, 89, 90, 180, 181, 200, 80, 99, 100, 180, 181, 220, 60, 89, 90, 180, 181, 200};
    private static final int[] from6to12Without = {60, 69, 70, 100, 101, 130, 80, 89, 90, 150, 151, 200, 60, 79, 80, 140, 141, 180};
    private static final int[] from13to19With = {60, 89, 90, 130, 131, 200, 80, 89, 90, 150, 151, 200, 60, 99, 100, 140, 141, 180};
    private static final int[] from13to19Without = {60, 69, 70, 100, 101, 130, 80, 99, 100, 130, 131, 180, 60, 79, 80, 120, 121, 160};
    private static final int[] from20With = {60, 99, 100, 130, 131, 200, 80, 129, 130, 200, 201, 250, 60, 99, 100, 180, 161, 230};
    private static final int[] from20Without = {60, 69, 70, 100, 101, 130, 80, 99, 100, 140, 141, 200, 60, 79, 80, 120, 121, 180};

    public static int bloodSugarLevel(Context context, int measurementMode, int value) {

        boolean isDiabetes = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Config.IS_DIABETES, false);
        boolean isPregnant = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Config.IS_PREGNANT, false);
        int ageRange = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(Config.AGE_RANGE, 1);

        int[] category = null;
        int sugarlevel = 0;

        /* Selecting category. */
        if (isPregnant) {
            category = isDiabetes ? pregnantWith : pregnantWithout;
        } else {
            switch (ageRange) {
                case 1: // less than 6
                    if (isDiabetes) category = less6With;
                    else category = less6Without;
                    break;
                case 2: // 6 to 12
                    if (isDiabetes) category = from6to12With;
                    else category = from6to12Without;
                    break;
                case 3: // 13 to 19
                    if (isDiabetes) category = from13to19With;
                    else category = from13to19Without;
                    break;
                case 4: // 19+
                    if (isDiabetes) category = from20With;
                    else category = from20Without;
                    break;
            }
        }

        assert category != null;

        int batasBawahLow = category[6 * (measurementMode - 1)];
        if (value > batasBawahLow)
            sugarlevel = 1;

        int batasBawahNormal = category[6 * (measurementMode - 1) + 2];
        if (value > batasBawahNormal)
            sugarlevel = 2;

        int batasBawahHigh = category[6 * (measurementMode - 1) + 4];
        if (value > batasBawahHigh)
            sugarlevel = 3;


        int batasAtasHigh = category[6 * (measurementMode - 1) + 5];
        if (value > batasAtasHigh)
            sugarlevel = 4;

        /**
         * 0 = very low
         * 1 = low
         * 2 = normal
         * 3 = high
         * 4 = very high
         **/
        return sugarlevel;
    }

    public static String whatCategory(boolean isPregnant, int ageRange, boolean isWithDiabetes) {

        String category = "";

        /* Selecting category. */
        if (isPregnant) {
            if (isWithDiabetes) category = "pregnantWith";
            else category = "pregnantWithout";


        } else {
            /* */
            switch (ageRange) {
                case 1: // less than 6
                    if (isWithDiabetes) category = "less6With";
                    else category = "less6Without";
                    break;
                case 2: // 6 to 12
                    if (isWithDiabetes) category = "from6to12With";
                    else category = "from6to12Without";
                    break;
                case 3: // 13 to 19
                    if (isWithDiabetes) category = "from13to19With";
                    else category = "from13to19Without";
                    break;
                case 4: // 19+
                    if (isWithDiabetes) category = "from20With";
                    else category = "from20Without";
                    break;
            }
        }

        return category;
    }

    int lowerLimit(boolean isPregnant, int ageRange, boolean isDiabetes, int measurementMode, int concentrationRange) {

        int[] category = new int[]{};

        /* Selecting category. */
        if (isPregnant) {
            if (isDiabetes) category = pregnantWith;
            else category = pregnantWithout;


        } else {
            /* */
            switch (ageRange) {
                case 1: // less than 6
                    if (isDiabetes) category = less6With;
                    else category = less6Without;
                    break;
                case 2: // 6 to 12
                    if (isDiabetes) category = from6to12With;
                    else category = from6to12Without;
                    break;
                case 3: // 13 to 19
                    if (isDiabetes) category = from13to19With;
                    else category = from13to19Without;
                    break;
                case 4: // 19+
                    if (isDiabetes) category = from20With;
                    else category = from20Without;
                    break;
            }
        }

        /* Determining index. */
        int index = (2 * measurementMode * concentrationRange) - 1;
        return category[index];
    }

    int upperLimit(boolean isPregnant, int ageRange, boolean isDiabetes, int measurementMode, int concentrationRange) {
        int[] category = new int[]{};

        /* Selecting category. */
        if (isPregnant) {
            if (isDiabetes) category = pregnantWith;
            else category = pregnantWithout;
        } else {
            switch (ageRange) {
                case 1: // less than 6
                    if (isDiabetes) category = less6With;
                    else category = less6Without;
                    break;
                case 2: // 6 to 12
                    if (isDiabetes) category = from6to12With;
                    else category = from6to12Without;
                    break;
                case 3: // 13 to 19
                    if (isDiabetes) category = from13to19With;
                    else category = from13to19Without;
                    break;
                case 4: // 19+
                    if (isDiabetes) category = from20With;
                    else category = from20Without;
                    break;
            }
        }

        /* Determining index. */
        int index = (2 * measurementMode * concentrationRange);
        return category[index];
    }
}
