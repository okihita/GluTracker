package com.okihita.glutracker.ViewBase;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.okihita.glutracker.R;
import com.okihita.glutracker.util.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private RequestQueue mRequestQueue;
    private ProgressBar mProgressBar;

    private TextView mNameField;
    private TextView mBirthdateField;
    private TextView mGenderField;
    private TextView mAgeField;
    private TextView mEmailField;
    private TextView mHistoryField;
    private TextView mPasswordField;

    private Button mEditButton;
    private int mLoggedInUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        mLoggedInUserId = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                .getInt(Config.LOGGED_IN_USER_ID, 0);
        Log.d(Config.TAG, "Logged in user ID:" + String.valueOf(mLoggedInUserId));
        fetchUserInfo(); // contacting server to get user information
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mProgressBar = (ProgressBar) view.findViewById(R.id.profile_ProgressBar_fullscreen);

        mNameField = (TextView) view.findViewById(R.id.profile_TextView_name);
        mBirthdateField = (TextView) view.findViewById(R.id.profile_TextView_birthdate);
        mGenderField = (TextView) view.findViewById(R.id.profile_TextView_gender);
        mAgeField = (TextView) view.findViewById(R.id.profile_TextView_age);
        mEmailField = (TextView) view.findViewById(R.id.profile_TextView_email);
        mHistoryField = (TextView) view.findViewById(R.id.profile_TextView_history);
        mPasswordField = (TextView) view.findViewById(R.id.profile_TextView_password);

        mEditButton = (Button) view.findViewById(R.id.ProfFrag_Button_edit);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragmentContainer, new ProfileEditorFragment()).commit();
            }
        });

        return view;
    }

    /**
     * Fetching user information from server.
     */
    private void fetchUserInfo() {

        /* Building request query. */
        Uri.Builder mBaseUriBuilder = (new Uri.Builder()).scheme("http")
                .authority(Config.BASE_URL)
                .appendPath(Config.SUBDOMAIN_ADDRESS)
                .appendPath(Config.USER_ENTRY_POINT)
                .appendQueryParameter("id", String.valueOf(mLoggedInUserId));
        String userInfoQuery = mBaseUriBuilder.build().toString();
        Log.d(Config.TAG, "Query: " + userInfoQuery);

        /* Requesting the query. */
        JsonArrayRequest measurementRequest = new JsonArrayRequest(
                userInfoQuery,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        /* Hide the progress bar, set the text fields' content to the response. */
                        mProgressBar.setVisibility(View.GONE);
                        Log.d(Config.TAG, "Success: " + String.valueOf(response));
                        try {
                            JSONObject object = response.getJSONObject(0);
                            mNameField.setText(": " + object.getString("name"));
                            mBirthdateField.setText(": " + object.getString("birthdate"));
                            mGenderField.setText(": " + ((object.getInt("gender") == 0) ? "Male" : "Female"));
                            mEmailField.setText(": " + object.getString("email"));
                            mHistoryField.setText(": " + ((object.getInt("history") == 0) ? "Without Diabetes" : "With Diabetes"));
                            mPasswordField.setText(": " + (object.getString("password")).replaceAll(".", "*"));

                            // Calculate date of birth
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                            Calendar dob = Calendar.getInstance();
                            dob.setTime(sdf.parse(object.getString("birthdate")));
                            Calendar today = Calendar.getInstance();
                            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                            if (today.get(Calendar.DAY_OF_YEAR) <= dob.get(Calendar.DAY_OF_YEAR))
                                age--;
                            mAgeField.setText(": " + age);

                            updateSharedPreference(object.getString("name"), age, object.getInt("history") == 1);

                        } catch (JSONException | ParseException ignored) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Config.TAG, "Error: " + String.valueOf(error));
                    }
                });

        Log.d(Config.TAG, userInfoQuery);
        mRequestQueue.add(measurementRequest);
    }

    private void updateSharedPreference(String userName, int userAge, boolean isUserDiabetes) {

        // Full name. Will be shortened in Measurement's greeting
        // @see
        PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext())
                .edit().putString(Config.LOGGED_IN_USER_NAME, userName)
                .commit();

        // Is user pregnant?
        PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext())
                .edit().putInt(Config.AGE, userAge)
                .commit();

        // Age range
        int ageRange = 1;
        if (userAge > 6) ageRange = 2;
        if (userAge > 12) ageRange = 3;
        if (userAge > 19) ageRange = 4;
        PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext())
                .edit().putInt(Config.AGE_RANGE, ageRange)
                .commit();

        // Diabetes history
        PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext())
                .edit().putBoolean(Config.IS_DIABETES, isUserDiabetes)
                .commit();
    }
}