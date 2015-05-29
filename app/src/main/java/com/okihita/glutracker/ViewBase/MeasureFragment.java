package com.okihita.glutracker.ViewBase;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.filippudak.ProgressPieView.ProgressPieView;
import com.okihita.glutracker.R;
import com.okihita.glutracker.util.Config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MeasureFragment extends Fragment {

    private static final int REQUEST_CODE_MANUAL_INPUT = 1;
    private final int MODE_BUTTON_PREMEAL = 1;
    private final int MODE_BUTTON_POSTMEAL = 2;
    private final int MODE_BUTTON_RANDOM = 3;

    private RequestQueue mRequestQueue;
    private ProgressPieView mProgressPieView;

    private ImageButton mPremealModeButton;
    private ImageButton mPostmealModeButton;
    private ImageButton mRandomModeButton;
    private Button mStartButton;
    private Button mManualInput;
    private Button mSaveButton;
    private TextView mResultTV;

    private Runnable stringupdater;
    private final Handler mHandler = new Handler();
    private final int mInterval = 25;
    private int mProgressPercentage;

    /* Menentukan kapan merah-hijau-kuning. */
    private int mJenisPengukuran;
    private int mKadar;
    private Date mTanggalWaktu = new Date();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_measure, container, false);

        TextView mGreetingTextView = (TextView) view.findViewById(R.id.measure_TextView_username);
        mPremealModeButton = (ImageButton) view.findViewById(R.id.measure_Button_premeal);
        mPostmealModeButton = (ImageButton) view.findViewById(R.id.measure_Button_postmeal);
        mRandomModeButton = (ImageButton) view.findViewById(R.id.measure_Button_random);

        mProgressPieView = (ProgressPieView) view.findViewById(R.id.measure_progressPieView);
        mStartButton = (Button) view.findViewById(R.id.measure_Button_start);
        mManualInput = (Button) view.findViewById(R.id.measure_Button_manualInput);
        mResultTV = (TextView) view.findViewById(R.id.measure_TextView_resultText);
        mSaveButton = (Button) view.findViewById(R.id.measure_Button_save);

        /* Change username in greeting text. */
        String username = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(Config.LOGGED_IN_USER_NAME, "User");
        mGreetingTextView.setText("Hi, " + getFirstWord(username));

        /* Progress wheel styling. */
        mProgressPieView.setProgress(0);
        mProgressPieView.setText("READY");
        mProgressPieView.setProgressColor(0xFFDDDDDD);
        mProgressPieView.setBackgroundColor(0xFFCCCCCC);
        mProgressPieView.setStrokeColor(0xFFDDDDDD);
        mProgressPieView.setStrokeWidth(1);

        stringupdater = new Runnable() {
            @Override
            public void run() {
                mProgressPercentage++;
                mProgressPieView.setProgress(mProgressPercentage);
                mProgressPieView.setText(mProgressPercentage + "%");
                mResultTV.setText("Measuring... Please wait.");
                if (mProgressPercentage < 100)
                    mHandler.postDelayed(stringupdater, mInterval);
                else
                    finishMeasuring();
            }
        };

        mPremealModeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mJenisPengukuran = Config.MEASUREMENT_MODE_PREMEAL;
                prepareForMeasuring(MODE_BUTTON_PREMEAL, view);
                return true;
            }
        });

        mPostmealModeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mJenisPengukuran = Config.MEASUREMENT_MODE_POSTMEAL;
                prepareForMeasuring(MODE_BUTTON_POSTMEAL, view);
                return true;
            }
        });

        mRandomModeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mJenisPengukuran = Config.MEASUREMENT_MODE_RANDOM;
                prepareForMeasuring(MODE_BUTTON_RANDOM, view);
                return true;
            }
        });

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTanggalWaktu = new Date();
                mProgressPercentage = 0;
                stringupdater.run();
            }
        });

        mManualInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManualInputFragment f = ManualInputFragment.newInstance();
                f.setTargetFragment(MeasureFragment.this, REQUEST_CODE_MANUAL_INPUT);
                f.show(getFragmentManager(), "manual");
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToServer();
            }
        });
        return view;
    }

    private void prepareForMeasuring(int pressedButton, View view) {

        /* Change button states. */
        view.setPressed(true);
        mStartButton.setEnabled(true);

        /* Reset wheel color and result. */
        mProgressPieView.setProgress(0);
        mProgressPieView.setProgressColor(0xFFDDDDDD);
        mProgressPieView.setBackgroundColor(0xFFCCCCCC);
        mProgressPieView.setText("READY");
        mResultTV.setText("Click below to start measuring.");

        /* Disable other buttons. */
        switch (pressedButton) {
            case MODE_BUTTON_PREMEAL:
                mPostmealModeButton.setPressed(false);
                mRandomModeButton.setPressed(false);
                break;
            case MODE_BUTTON_POSTMEAL:
                mPremealModeButton.setPressed(false);
                mRandomModeButton.setPressed(false);
                break;
            case MODE_BUTTON_RANDOM:
                mPremealModeButton.setPressed(false);
                mPostmealModeButton.setPressed(false);
                break;
        }
    }

    private void finishMeasuring() {

        /* Reset button states. */
        mPremealModeButton.setPressed(false);
        mPostmealModeButton.setPressed(false);
        mRandomModeButton.setPressed(false);

        mPremealModeButton.setEnabled(true);
        mPostmealModeButton.setEnabled(true);
        mRandomModeButton.setEnabled(true);

        mStartButton.setEnabled(false);
        mSaveButton.setEnabled(true);

        int levelResult = new Random().nextInt(250) + 50;
        mKadar = levelResult;
        mProgressPieView.setText(String.valueOf(levelResult) + "mg/dL");

        String resultText;
        int sugarlevel = Config.bloodSugarLevel(getActivity().getApplicationContext(), mJenisPengukuran, mKadar);

        resultText = "Your blood sugar level is ";
        Random r = new Random();
        int idx;

        switch (sugarlevel) {
            case 1:
                resultText += "low\n";
                idx = r.nextInt(Config.commentLow.length);
                resultText += Config.commentLow[idx];
                mProgressPieView.setProgressColor(0xFFFFCA2D);
                break;
            case 2:
                resultText += "normal\n";
                idx = r.nextInt(Config.commentNormal.length);
                resultText += Config.commentNormal[idx];
                mProgressPieView.setProgressColor(0xFF4EBF63);
                break;
            case 3:
                resultText += "high\n";
                idx = r.nextInt(Config.commentHigh.length);
                resultText += Config.commentHigh[idx];
                mProgressPieView.setProgressColor(0xFFE52A1B);
                break;
        }

        /* Add comment. */
        resultText += "\n";

        mResultTV.setText(resultText);
    }

    private void saveToServer() {
        String sDate = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("id", "ID"))).format(mTanggalWaktu);

        /* Building request query. */
        Uri.Builder mBaseUriBuilder = (new Uri.Builder()).scheme("http")
                .authority(Config.BASE_URL)
                .appendPath(Config.SUBDOMAIN_ADDRESS)
                .appendPath(Config.MEASUREMENT_ADDITION_ENTRY_POINT)
                .appendQueryParameter("date", sDate)
                .appendQueryParameter("kadar", String.valueOf(mKadar))
                .appendQueryParameter("jenis", String.valueOf(mJenisPengukuran));
        String saveResultItemQuery = mBaseUriBuilder.build().toString();
        Log.d(Config.TAG, saveResultItemQuery);

        /* Sending request. */
        StringRequest saveResultItemRequest = new StringRequest(
                saveResultItemQuery,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Config.TAG, "OK");

                        /* Change fragment content here. */
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                        ft.replace(R.id.fragmentContainer, new LogbookFragment()).addToBackStack("logbook").commit();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Config.TAG, "FAIL: " + error.toString());
                    }
                }
        );

        mRequestQueue.add(saveResultItemRequest);
    }

    /* To show only user's first name. */
    private String getFirstWord(String text) {
        if (text.indexOf(' ') > -1)  // Check if there is more than one word.
            return text.substring(0, text.indexOf(' ')); // Extract first word.
        else
            return text; // Text is the first word itself
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Measure");
    }
}