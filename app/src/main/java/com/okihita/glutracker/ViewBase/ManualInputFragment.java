package com.okihita.glutracker.ViewBase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.okihita.glutracker.R;
import com.okihita.glutracker.util.Config;

public class ManualInputFragment extends DialogFragment {

    public static final String EXTRA_MEASUREMENT = "glucometer.measurement";
    private static final int REQUEST_CODE_TIME_PICKER = 3;
    private static final int REQUEST_CODE_DATE_PICKER = 4;

    public static Button datePickerButton;
    public static Button timePickerButton;

    private Button mPremealModeButton;
    private Button mPostmealModeButton;
    private Button mRandomModeButton;
    private EditText mValueEditText;

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) return;
        Intent i = new Intent();
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_manual_input_dialog, null);

        mPremealModeButton = (Button) v.findViewById(R.id.manual_Button_premeal);
        mPostmealModeButton = (Button) v.findViewById(R.id.manual_Button_postmeal);
        mRandomModeButton = (Button) v.findViewById(R.id.manual_Button_random);
        mValueEditText = (EditText) v.findViewById(R.id.manualInput_TextView_value);

        mPremealModeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                modeSelection(Config.MEASUREMENT_MODE_PREMEAL, v);
                return true;
            }
        });

        mPostmealModeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                modeSelection(Config.MEASUREMENT_MODE_POSTMEAL, v);
                return true;
            }
        });

        mRandomModeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                modeSelection(Config.MEASUREMENT_MODE_RANDOM, v);
                return true;
            }
        });

        mValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PreferenceManager.getDefaultSharedPreferences(
                        getActivity().getApplicationContext()).edit()
                        .putInt(Config.MANUAL_INPUT_VALUE, Integer.valueOf(s.toString())).commit();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // When the DatePicker is pressed, show a new TimePickerDialog
        datePickerButton = (Button) v.findViewById(R.id.input_Button_datePicker);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment f = new DatePickerFragment();
                f.setTargetFragment(ManualInputFragment.this, REQUEST_CODE_DATE_PICKER);
                f.show(getFragmentManager(), "datepicker");
            }
        });

        // When the TimePicker is pressed, show a new TimePickerDialog
        timePickerButton = (Button) v.findViewById(R.id.input_Button_timePicker);
        timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment f = new TimePickerFragment();
                f.setTargetFragment(ManualInputFragment.this, REQUEST_CODE_TIME_PICKER);
                f.show(getFragmentManager(), "timepicker");
            }
        });

        // For the first time, set the "Premeal" button to be selected, and type = 1.
        // Also value = 80.
        mPremealModeButton.setPressed(true);
        PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext()).edit()
                .putInt(Config.MANUAL_INPUT_TYPE, Config.MEASUREMENT_MODE_PREMEAL).commit();
        PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext()).edit()
                .putInt(Config.MANUAL_INPUT_VALUE, 80).commit();

        // Return the view, which is a new AlertDialog builder
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Manual Input")
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .create();
    }

    private void modeSelection(int selectedMode, View view) {
        // Kalau salah satu ditekan, tahan state tombol itu dan matikan state yang lainnya.
        view.setPressed(true);

        switch (selectedMode) {
            case Config.MEASUREMENT_MODE_PREMEAL:
                mPostmealModeButton.setPressed(false);
                mRandomModeButton.setPressed(false);
                break;
            case Config.MEASUREMENT_MODE_POSTMEAL:
                mPremealModeButton.setPressed(false);
                mRandomModeButton.setPressed(false);
                break;
            case Config.MEASUREMENT_MODE_RANDOM:
                mPremealModeButton.setPressed(false);
                mPostmealModeButton.setPressed(false);
                break;
        }

        PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext()).edit()
                .putInt(Config.MANUAL_INPUT_TYPE, selectedMode).commit();
    }

    public static ManualInputFragment newInstance() {
        return new ManualInputFragment();
    }
}
