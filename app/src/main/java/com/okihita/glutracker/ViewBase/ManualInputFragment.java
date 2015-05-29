package com.okihita.glutracker.ViewBase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.okihita.glutracker.R;

public class ManualInputFragment extends DialogFragment {

    public static final String EXTRA_MEASUREMENT = "glucometer.measurement";
    private static final int REQUEST_CODE_TIME_PICKER = 3;
    private static final int REQUEST_CODE_DATE_PICKER = 4;

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) return;
        Intent i = new Intent();
        // i.putExtra(EXTRA_MEASUREMENT, mRadius);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_manual_input_dialog, null);

        // When the DatePicker is pressed, show a new TimePickerDialog
        (v.findViewById(R.id.input_Button_datePicker)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment f = new DatePickerFragment();
                f.setTargetFragment(ManualInputFragment.this, REQUEST_CODE_DATE_PICKER);
                f.show(getFragmentManager(), "datepicker");
            }
        });

        // When the TimePicker is pressed, show a new TimePickerDialog
        (v.findViewById(R.id.input_Button_timePicker)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment f = new TimePickerFragment();
                f.setTargetFragment(ManualInputFragment.this, REQUEST_CODE_TIME_PICKER);
                f.show(getFragmentManager(), "timepicker");
            }
        });

        // Return the view, which is a new AlertDialog builder
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Manual Input")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .create();

    }

    public static ManualInputFragment newInstance() {
        return new ManualInputFragment();
    }
}
