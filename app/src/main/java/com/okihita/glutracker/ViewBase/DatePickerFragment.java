package com.okihita.glutracker.ViewBase;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.DatePicker;

import com.okihita.glutracker.util.Config;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    // DON'T FORGET THAT MONTH STARTS FROM ZERO
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        Log.d(Config.TAG, "" + year + " " + (month + 1) + " " + day);

        PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext()).edit()
                .putInt(Config.MANUAL_INPUT_YEAR, year).commit();

        PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext()).edit()
                .putInt(Config.MANUAL_INPUT_MONTH, month + 1).commit();

        PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext()).edit()
                .putInt(Config.MANUAL_INPUT_DAY, day).commit();

        ManualInputFragment.datePickerButton.setText(year + "-" + (month + 1) + "-" + day);
    }
}