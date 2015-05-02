package com.okihita.glutracker.ViewSplash;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.okihita.glutracker.R;
import com.okihita.glutracker.model.MeasurementItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SplashFragment extends Fragment {

    TextView mTextView;
    JSONArray mDataJSONArray;
    List<MeasurementItem> mItems = new ArrayList<>();
    int hai = 0;
    int mInterval = 25;
    Runnable stringupdater;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        mTextView = (TextView) view.findViewById(R.id.splash_TextView_internet);
        final TextView mTimer = (TextView) view.findViewById(R.id.timertester);

        stringupdater = new Runnable() {
            @Override
            public void run() {
                hai++;
                // mTimer.setText(String.valueOf(hai));
                if (hai < 1000) mHandler.postDelayed(stringupdater, mInterval);
            }
        };

        stringupdater.run();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = "http://192.168.137.1/urs";

        // Request a string response from the provided URL.
        JsonArrayRequest stringRequest = new JsonArrayRequest(
                url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mDataJSONArray = response;
                        String dataString = "";
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object = response.getJSONObject(i);
                                dataString += "Date: " + object.getString("date") + "\n";
                                dataString += "Jenis: ";
                                switch (object.getInt("jenis")) {
                                    case 0:
                                        dataString += "Premeal";
                                        break;
                                    case 1:
                                        dataString += "Postmeal";
                                        break;
                                    case 2:
                                        dataString += "Random";
                                        break;
                                }
                                dataString += "\n" + "Kadar: " + object.getInt("kadar") + "\n" + "\n";
                            }
                        } catch (JSONException ignored) {
                        }
                        // mTextView.setText(dataString);
                        mTextView.setText("");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // mTextView.setText("That didn't work!");
                        mTextView.setText("");
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        Log.d("Vol", "Queue added!");
        return view;
    }
}
