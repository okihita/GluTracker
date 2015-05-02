package com.okihita.glutracker.ViewSplash;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.okihita.glutracker.R;
import com.okihita.glutracker.ViewBase.BaseActivity;
import com.okihita.glutracker.util.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupFragment extends Fragment {

    ArrayAdapter<CharSequence> dateAdapter;
    List<CharSequence> dateArrayList = new ArrayList<>();
    RequestQueue mRequestQueue;
    private LinearLayout pregnantSwitchView;
    private int numberOfDays = 30;
    private Spinner mSpinnerYear;
    private Spinner mSpinnerMonth;
    private Spinner mSpinnerDate;

    // Input widgets
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordVerifyText;

    private EditText mFullnameEditText;
    private int mGender = 1;
    private int isPregnant = 0;
    private int isWithHistory = 0;

    public static boolean isEmailValid(String email) {
        boolean isValid = false;
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) isValid = true;
        return isValid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        Button signupButton = (Button) view.findViewById(R.id.FS_Button_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPasswordEditText.getText().toString().equals(mPasswordVerifyText.getText().toString())) {
                    Toast.makeText(getActivity(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
                } else if (mEmailEditText.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Please type your email.", Toast.LENGTH_SHORT).show();
                } else {
                    checkSignup();
                }
            }
        });

        mEmailEditText = (EditText) view.findViewById(R.id.signup_email_field);
        mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isEmailValid(s.toString())) {
                    mEmailEditText.setTextColor(0xFF882222);
                } else {
                    mEmailEditText.setTextColor(0xFF228822);
                }
            }
        });

        mPasswordEditText = (EditText) view.findViewById(R.id.signup_password_field);
        mPasswordVerifyText = (EditText) view.findViewById(R.id.signup_verify_password_field);
        mFullnameEditText = (EditText) view.findViewById(R.id.signup_fullname_field);

        /* Pregnant switch. */
        pregnantSwitchView = (LinearLayout) view.findViewById(R.id.FS_Linear_pregnant);
        Switch pregnantSwitch = (Switch) view.findViewById(R.id.FS_Switch_pregnant);
        if (pregnantSwitch != null) {
            pregnantSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    isPregnant = (b ? 1 : 0);
                }
            });
        }

        Switch historySwitch = (Switch) view.findViewById(R.id.FS_Switch_history);
        if (historySwitch != null) {
            historySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    isWithHistory = (b ? 1 : 0);
                }
            });
        }

        RadioButton maleRadio = (RadioButton) view.findViewById(R.id.FS_Radio_male);
        maleRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGender = 0;
                pregnantSwitchView.setVisibility(View.GONE);
            }
        });

        RadioButton femaleRadio = (RadioButton) view.findViewById(R.id.FS_Radio_female);
        femaleRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGender = 1;
                pregnantSwitchView.setVisibility(View.VISIBLE);
            }
        });

        /* Year-related dropdown spinner. */
        mSpinnerYear = (Spinner) view.findViewById(R.id.yearSpinner);
        List<CharSequence> yearArrayList = new ArrayList<>();
        for (int i = 1943; i < 2010; i++)
            yearArrayList.add(String.valueOf(i));

        ArrayAdapter<CharSequence> yearAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, yearArrayList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerYear.setAdapter(yearAdapter);

        /* Month-related dropdown spinner. */
        mSpinnerMonth = (Spinner) view.findViewById(R.id.monthSpinner);
        List<CharSequence> monthArrayList = new ArrayList<>();
        monthArrayList.add("January");
        monthArrayList.add("February");
        monthArrayList.add("March");
        monthArrayList.add("April");
        monthArrayList.add("May");
        monthArrayList.add("June");
        monthArrayList.add("July");
        monthArrayList.add("August");
        monthArrayList.add("September");
        monthArrayList.add("October");
        monthArrayList.add("November");
        monthArrayList.add("December");

        ArrayAdapter<CharSequence> monthAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, monthArrayList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerMonth.setAdapter(monthAdapter);

        mSpinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // JAN
                        numberOfDays = 31;
                        break;
                    case 1: // FEB
                        int year = Integer.parseInt(mSpinnerYear.getSelectedItem().toString());
                        if (year % 100 == 0) {
                            if (year % 400 == 0) {
                                numberOfDays = 29;
                            } else {
                                numberOfDays = 28;
                            }
                        } else if (year % 4 == 0) {
                            numberOfDays = 29;
                        } else {
                            numberOfDays = 28;
                        }
                        break;
                    case 2: // MAR
                        numberOfDays = 31;
                        break;
                    case 3: // APR
                        numberOfDays = 30;
                        break;
                    case 4: // MAY
                        numberOfDays = 31;
                        break;
                    case 5: // JUN
                        numberOfDays = 30;
                        break;
                    case 6: // JUL
                        numberOfDays = 31;
                        break;
                    case 7: // AUG
                        numberOfDays = 31;
                        break;
                    case 8: // SEP
                        numberOfDays = 30;
                        break;
                    case 9: // OCT
                        numberOfDays = 31;
                        break;
                    case 10: // NOV
                        numberOfDays = 30;
                        break;
                    case 11: // DEC
                        numberOfDays = 31;
                        break;
                }

                dateArrayList.clear();
                for (int i = 1; i <= numberOfDays; i++)
                    dateArrayList.add(String.valueOf(i));

                dateAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* Date-related dropdown spinner. */
        mSpinnerDate = (Spinner) view.findViewById(R.id.dateSpinner);
        dateAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, dateArrayList);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerDate.setAdapter(dateAdapter);

        return view;
    }

    private void checkSignup() {

        String birthdate = mSpinnerYear.getSelectedItem().toString() + "-" +
                ("00" + String.valueOf(1 + mSpinnerMonth.getSelectedItemPosition()))
                        .substring(String.valueOf(1 + mSpinnerMonth.getSelectedItemPosition()).length())
                + "-"
                + ("00" + String.valueOf(1 + mSpinnerDate.getSelectedItemPosition()))
                .substring(String.valueOf(1 + mSpinnerDate.getSelectedItemPosition()).length());

        /* Building request query. */
        Uri.Builder mBaseUriBuilder =
                (new Uri.Builder()).scheme("http")
                        .authority(Config.BASE_URL)
                        .appendPath(Config.SUBDOMAIN_ADDRESS)
                        .appendPath(Config.SIGNUP_ENTRY_POINT)
                        .appendQueryParameter("email", mEmailEditText.getText().toString())
                        .appendQueryParameter("pass", mPasswordEditText.getText().toString())
                        .appendQueryParameter("name", mFullnameEditText.getText().toString())
                        .appendQueryParameter("birthdate", birthdate)
                        .appendQueryParameter("gender", String.valueOf(mGender))
                        .appendQueryParameter("ispregnant", String.valueOf(isPregnant))
                        .appendQueryParameter("history", String.valueOf(isWithHistory));
        String userSignupQuery = mBaseUriBuilder.build().toString();

        Log.d("Itin", userSignupQuery);

        // Build the request
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                userSignupQuery,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        /*
                        * If the operation success, this will return the ID of the newly-
                        * created user. If it fails, it will return 0.
                        * */
                        Log.d("TAG", response);
                        if ((response.substring(1)).equals("0")) {
                            Toast.makeText(getActivity(), "Email already registered.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Successfully registered.", Toast.LENGTH_SHORT).show();
                            PreferenceManager.getDefaultSharedPreferences(
                                    getActivity().getApplicationContext()).edit()
                                    .putInt(Config.LOGGED_IN_USER_ID, Integer.valueOf(response.substring(1))).commit();
                            startActivity(new Intent(getActivity(), BaseActivity.class));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "Error: " + error.toString());
                Toast.makeText(getActivity(), "Network error.\nPlease try again.", Toast.LENGTH_SHORT).show();
            }
        });
        // Add the request to the RequestQueue.
        mRequestQueue.add(stringRequest);
    }
}
