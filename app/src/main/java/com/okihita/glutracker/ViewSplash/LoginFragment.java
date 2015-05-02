package com.okihita.glutracker.ViewSplash;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.okihita.glutracker.R;
import com.okihita.glutracker.ViewBase.BaseActivity;
import com.okihita.glutracker.util.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class LoginFragment extends Fragment {

    EditText mUsernameField;
    RequestQueue mRequestQueue;
    EditText mPasswordField;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mUsernameField = (EditText) view.findViewById(R.id.FLS_EditText_username);
        mPasswordField = (EditText) view.findViewById(R.id.FLS_EditText_password);
        Button loginButton = (Button) view.findViewById(R.id.FLS_Button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });

        return view;
    }

    private void checkLogin() {

        /* Building request query. */
        Uri.Builder mBaseUriBuilder =
                (new Uri.Builder()).scheme("http")
                        .authority(Config.BASE_URL)
                        .appendPath(Config.SUBDOMAIN_ADDRESS)
                        .appendPath(Config.USER_ENTRY_POINT)
                        .appendQueryParameter("email", mUsernameField.getText().toString());
        String userLoginQuery = mBaseUriBuilder.build().toString();

        JsonArrayRequest userLoginRequest = new JsonArrayRequest(
                userLoginQuery,
                new Response.Listener<JSONArray>() {

                    /* Response. */
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(Config.TAG, "RESPONSE: " + response.toString());
                        try {

                            JSONObject object = response.getJSONObject(0);

                            /* If passwords match, update Shared Preference and go to main screen. */
                            if (object.getString("password").equals(mPasswordField.getText().toString())) {

                                // Calculate date of birth
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                Calendar dob = Calendar.getInstance();
                                dob.setTime(sdf.parse(object.getString("birthdate")));
                                Calendar today = Calendar.getInstance();
                                int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                                if (today.get(Calendar.DAY_OF_YEAR) <= dob.get(Calendar.DAY_OF_YEAR))
                                    age--;

                                updateSharedPreference(object.getInt("id"), object.getString("name"), age, object.getInt("history") == 1);
                                startActivity(new Intent(getActivity(), BaseActivity.class));

                            /* Jika tidak, berikan pemberitahuan surel atau sandi salah. */
                            } else {
                                Toast.makeText(getActivity(), "Wrong email or password.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException ignored) {
                            Toast.makeText(getActivity(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                        } catch (ParseException ignored) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        mRequestQueue.add(userLoginRequest);
    }


    private void updateSharedPreference(int userId, String userName, int userAge, boolean isUserDiabetes) {

        // User ID
        PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext()).edit()
                .putInt(Config.LOGGED_IN_USER_ID, userId).commit();

        // Full name. Will be shortened in Measurement's greeting
        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                .edit().putString(Config.LOGGED_IN_USER_NAME, userName).commit();

        // Is user pregnant?
        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                .edit().putInt(Config.AGE, userAge).commit();

        // Age range
        int ageRange = 1;
        if (userAge > 6) ageRange = 2;
        if (userAge > 12) ageRange = 3;
        if (userAge > 19) ageRange = 4;

        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                .edit().putInt(Config.AGE_RANGE, ageRange).commit();

        // Diabetes history
        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext())
                .edit().putBoolean(Config.IS_DIABETES, isUserDiabetes).commit();
    }
}
