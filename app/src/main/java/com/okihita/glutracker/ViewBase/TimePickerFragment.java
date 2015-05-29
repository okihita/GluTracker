package com.okihita.glutracker.ViewBase;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import com.okihita.glutracker.util.Config;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        Log.d(Config.TAG, "" + hourOfDay + " " + minute);

        PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext()).edit()
                .putInt(Config.MANUAL_INPUT_HOUR, hourOfDay).commit();

        PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext()).edit()
                .putInt(Config.MANUAL_INPUT_MINUTE, minute).commit();

        ManualInputFragment.timePickerButton.setText(hourOfDay + ":" + minute);
    }
}