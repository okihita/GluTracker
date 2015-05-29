package com.okihita.glutracker.ViewBase;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
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
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.filippudak.ProgressPieView.ProgressPieView;
import com.okihita.glutracker.R;
import com.okihita.glutracker.util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class MeasureFragment extends Fragment {

    private static final int REQUEST_CODE_MANUAL_INPUT = 1;
    private final int MODE_BUTTON_PREMEAL = 1;
    private final int MODE_BUTTON_POSTMEAL = 2;
    private final int MODE_BUTTON_RANDOM = 3;

    private RequestQueue mRequestQueue;
    private ProgressPieView mProgressPieView;
    private String mDateString;

    private ImageButton mPremealModeButton;
    private ImageButton mPostmealModeButton;
    private ImageButton mRandomModeButton;
    private Button mStartButton;
    private Button mSaveButton;
    private TextView mResultTV;

    private Runnable stringupdater;
    private final Handler mHandler = new Handler();
    private final int mInterval = 300;
    private int mProgressPercentage;

    /* Menentukan kapan merah-hijau-kuning. */
    private int mJenisPengukuran;
    private int mKadar;
    private Date mTanggalWaktu = new Date();

    /* Bluetooth-related variables. */
    int receivedValue = 0;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    /* Function to show only user's first name. */
    private String getFirstWord(String text) {
        if (text.indexOf(' ') > -1)  // Check if there is more than one word.
            return text.substring(0, text.indexOf(' ')); // Extract first word.
        else
            return text; // Text is the first word itself
    }

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

        // Update pie animation.
        stringupdater = new Runnable() {
            @Override
            public void run() {
                mProgressPercentage++;
                mProgressPieView.setProgress(mProgressPercentage);
                mProgressPieView.setText(mProgressPercentage + "%");
                mResultTV.setText("Measuring... Please wait.");
                if (mProgressPercentage < 100)

                    // Every mInterval, add the percentage by 1.
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
                try {
                    findBT();
                    openBT();
                } catch (IOException ignored) {
                }
            }
        });

        view.findViewById(R.id.measure_Button_manualInput).setOnClickListener(new View.OnClickListener() {
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
                mDateString = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("id", "ID"))).format(mTanggalWaktu);
                saveResultToServer();
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

        // int levelResult = new Random().nextInt(250) + 50;
        mKadar = receivedValue;
        mProgressPieView.setText(String.valueOf(receivedValue) + "mg/dL");

        String resultText = "Your blood sugar level is ";
        int sugarlevel = Config.bloodSugarLevel(getActivity().getApplicationContext(), mJenisPengukuran, mKadar);

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

        /* Add comment to result text. */
        resultText += "\n";
        mResultTV.setText(resultText);
    }

    private void saveResultToServer() {

        Log.d(Config.TAG, "Save result to server called!");

        /* Building request query. */
        Uri.Builder mBaseUriBuilder = (new Uri.Builder()).scheme("http")
                .authority(Config.BASE_URL)
                .appendPath(Config.SUBDOMAIN_ADDRESS)
                .appendPath(Config.MEASUREMENT_ADDITION_ENTRY_POINT)
                .appendQueryParameter("date", mDateString)
                .appendQueryParameter("kadar", String.valueOf(mKadar))
                .appendQueryParameter("jenis", String.valueOf(mJenisPengukuran));
        String saveResultItemQuery = mBaseUriBuilder.build().toString();

        Log.d(Config.TAG, "Query was:" + saveResultItemQuery);

        /* Sending request. */
        StringRequest saveResultItemRequest = new StringRequest(
                saveResultItemQuery,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                    }
                }
        );

        mRequestQueue.add(saveResultItemRequest);
    }

    /* Bluetooth commands. 1/3: FIND. */
    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // myLabel.setText("No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")) {
                    mmDevice = device;
                    break;
                }
            }
        }
        // myLabel.setText("Bluetooth Device Found");
    }

    /* Bluetooth commands. 2/3: OPEN. */
    void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();
        // myLabel.setText("Bluetooth Opened");
    }

    /* Bluetooth commands. 3/3: LISTEN. */
    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            // myLabel.setText(data);
                                            receivedValue = Integer.valueOf(data);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Measure");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(getActivity(), "Measurement saved!", Toast.LENGTH_SHORT).show();
            Log.d(Config.TAG, "Activity result called!");

            // Setup yang mau disimpan.
            mKadar = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Config.MANUAL_INPUT_VALUE, 0);
            mJenisPengukuran = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Config.MANUAL_INPUT_TYPE, 0);
            String year = String.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Config.MANUAL_INPUT_YEAR, 0));
            String month = String.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Config.MANUAL_INPUT_MONTH, 0));
            String date = String.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Config.MANUAL_INPUT_DAY, 0));
            String hour = String.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Config.MANUAL_INPUT_HOUR, 0));
            String minute = String.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Config.MANUAL_INPUT_MINUTE, 0));
            mDateString = year + "-" + month + "-" + date + " " + hour + ":" + minute + ":00";

            // Panggil save to server
            saveResultToServer();

        }
    }
}