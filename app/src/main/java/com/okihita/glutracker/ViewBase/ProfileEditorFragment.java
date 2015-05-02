package com.okihita.glutracker.ViewBase;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.okihita.glutracker.R;
import com.okihita.glutracker.util.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileEditorFragment extends Fragment {

    private RequestQueue mRequestQueue;
    private TextView mNameField;
    private TextView mBirthdateField;
    private TextView mGenderField;
    private TextView mEmailField;
    private int mLoggedInUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        mLoggedInUserId = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                .getInt(Config.LOGGED_IN_USER_ID, 0);
        fetchUserInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_editor, container, false);

        mNameField = (TextView) view.findViewById(R.id.profileEditor_TextView_name);
        mBirthdateField = (TextView) view.findViewById(R.id.profileEditor_TextView_birthdate);
        mGenderField = (TextView) view.findViewById(R.id.profileEditor_TextView_gender);
        mEmailField = (TextView) view.findViewById(R.id.profileEditor_TextView_email);
        view.findViewById(R.id.profileEditor_Button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserInfo();
            }
        });

        return view;
    }

    private void updateUserInfo() {
        final String requestString = Config.BASE_URL
                + "edituser.php?id=" + mLoggedInUserId
                + "&name=" + mNameField.getText()
                + "&pass=hai"
                + "&date=" + mBirthdateField.getText();

        StringRequest stringRequest = new StringRequest(
                requestString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Config.TAG, requestString + " | | | " + response);
                        Toast.makeText(getActivity(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Config.TAG, requestString + "|" + error.toString());
                    }
                });

        mRequestQueue.add(stringRequest);
    }


    private void fetchUserInfo() {
        /* Building request query. */
        Uri.Builder mBaseUriBuilder = (new Uri.Builder()).scheme("http")
                .authority(Config.BASE_URL)
                .appendPath(Config.SUBDOMAIN_ADDRESS)
                .appendPath(Config.USER_ENTRY_POINT)
                .appendQueryParameter("id", String.valueOf(mLoggedInUserId));
        String userInfoQuery = mBaseUriBuilder.build().toString();
        Log.d(Config.TAG, "Query: " + userInfoQuery);

        JsonArrayRequest measurementRequest = new JsonArrayRequest(
                userInfoQuery,
                new Response.Listener<JSONArray>() {

                    /* Response. */
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(Config.TAG, "Success: " + String.valueOf(response));
                        try {
                            JSONObject object = response.getJSONObject(0);
                            mNameField.setText(object.getString("name"));
                            mBirthdateField.setText(object.getString("birthdate"));
                            mGenderField.setText(": " + ((object.getInt("gender") == 0) ? "Male" : "Female"));
                            mEmailField.setText(object.getString("email"));

                            // Edit the shared preference
                            PreferenceManager.getDefaultSharedPreferences(
                                    getActivity().getApplicationContext()).edit()
                                    .putString(Config.LOGGED_IN_USER_NAME, object.getString("name")).commit();

                        } catch (JSONException ignored) {
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Config.TAG, "Fail: " + String.valueOf(error));
                    }
                });

        mRequestQueue.add(measurementRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Edit Profile");
    }
}