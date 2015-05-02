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


public class LoginFragment extends Fragment {

    EditText mUsernameField;
    RequestQueue mRequestQueue;
    EditText mPasswordField;

    String mReceivedPassword;
    int mReceivedUserId;

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
                            mReceivedPassword = response.getJSONObject(0).getString("password");
                            mReceivedUserId = response.getJSONObject(0).getInt("id");

                            /* Jika sandi cocok, masukkan id ke dalam Shared Preferences, lalu masuk ke menu. */
                            if (mReceivedPassword.equals(mPasswordField.getText().toString())) {
                                PreferenceManager.getDefaultSharedPreferences(
                                        getActivity().getApplicationContext()).edit()
                                        .putInt(Config.LOGGED_IN_USER_ID, mReceivedUserId).commit();
                                Log.d(Config.TAG, String.valueOf(mReceivedUserId));

                                startActivity(new Intent(getActivity(), BaseActivity.class));

                            /* Jika tidak, berikan pemberitahuan surel atau sandi salah. */
                            } else {
                                Toast.makeText(getActivity(), "Wrong email or password.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException ignored) {
                            Toast.makeText(getActivity(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        mRequestQueue.add(userLoginRequest);
    }
}
