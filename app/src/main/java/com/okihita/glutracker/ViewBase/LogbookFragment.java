package com.okihita.glutracker.ViewBase;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.okihita.glutracker.R;
import com.okihita.glutracker.model.ItemAdapter;
import com.okihita.glutracker.model.MeasurementItem;
import com.okihita.glutracker.util.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class LogbookFragment extends Fragment {

    private ListView mMeasurementListView;
    private RequestQueue mRequestQueue;
    private ArrayList<MeasurementItem> mMeasurementItems = new ArrayList<>();
    private ArrayList<MeasurementItem> mPartialMeasurementItems = new ArrayList<>();
    private ItemAdapter mItemAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        fetchMeasurementItems();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logbook, container, false);

        Button addButton = (Button) view.findViewById(R.id.logbook_Button_add);
        Button sendButton = (Button) view.findViewById(R.id.logbook_Button_send);
        Button viewGraphButton = (Button) view.findViewById(R.id.logbook_Button_viewGraph);
        mMeasurementListView = (ListView) view.findViewById(R.id.logbook_ListView_list);

        mItemAdapter = new ItemAdapter(this, mMeasurementItems);
        mMeasurementListView.setAdapter(mItemAdapter);

        view.findViewById(R.id.logbook_Button_premeal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repopulateList(Config.MEASUREMENT_MODE_PREMEAL);
            }
        });

        view.findViewById(R.id.logbook_Button_postmeal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repopulateList(Config.MEASUREMENT_MODE_POSTMEAL);
            }
        });

        view.findViewById(R.id.logbook_Button_random).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repopulateList(Config.MEASUREMENT_MODE_RANDOM);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainer, new MeasureFragment()).addToBackStack("measure").commit();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Get user values from SharedPreferences. */
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                boolean isPregnant = sp.getBoolean(Config.IS_PREGNANT, false);
                int ageRange = sp.getInt(Config.AGE_RANGE, 1);
                boolean isWithHistory = sp.getBoolean(Config.IS_DIABETES, false);
                String username = sp.getString(Config.LOGGED_IN_USER_NAME, "Username");
                int age = sp.getInt(Config.AGE, 20);

                /* Setup the sharing. */
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");

                String shareBody = "Data Pengukuran 7 Hari Terakhir\n" + username + "\n";
                /* Cek semua item dalam list.
                * Jika tanggal pengambilan termasuk dalam tujuh hari ke belakang, tampilkan. */
                String currentDate = "";
                for (int i = 0; i < mMeasurementItems.size(); i++) {
                    MeasurementItem mi = mMeasurementItems.get(i);
                    String itemDate = (new SimpleDateFormat("EEE, d MMM yyyy")).format(mi.getTanggalAmbil());
                    // If the date is different, tampilkan tanggalnya
                    if (!currentDate.equals(itemDate)) {
                        currentDate = itemDate;
                        shareBody += "\n" + itemDate + "\n";
                    }
                    shareBody += (new SimpleDateFormat("HH:mm")).format(mi.getTanggalAmbil());
                    shareBody += " - (" + mi.getJenisTeks().toLowerCase() + ") " + mi.getKadar() + "mg/dL ";

                    /**
                     * 0 = very low
                     * 1 = low
                     * 2 = normal
                     * 3 = high
                     * 4 = very high
                     **/
                    switch (Config.bloodSugarLevel(getActivity().getApplicationContext(), mi.getJenis(), mi.getKadar())) {
                        case 0:
                            shareBody += "(very low)";
                            break;
                        case 1:
                            shareBody += "(low)";
                            break;
                        case 2:
                            shareBody += "(normal)";
                            break;
                        case 3:
                            shareBody += "(high)";
                            break;
                        case 4:
                            shareBody += "(very high)";
                            break;
                    }
                    shareBody += "\n";
                }

                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, username + ", " + age + " tahun");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Kirim dengan"));
            }
        });

        viewGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Change current fragment to GraphFragment. */
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragmentContainer, new GraphFragment())
                        .commit();
            }
        });

        return view;
    }

    private void fetchMeasurementItems() {

        /* Building request query. */
        Uri.Builder mBaseUriBuilder = (new Uri.Builder()).scheme("http")
                .authority(Config.BASE_URL)
                .appendPath(Config.SUBDOMAIN_ADDRESS);
        String logbookRequest = mBaseUriBuilder.build().toString();

        JsonArrayRequest measurementRequest = new JsonArrayRequest(
                logbookRequest,
                new Response.Listener<JSONArray>() {

                    /* Response. */
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            mMeasurementItems.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object = response.getJSONObject(i);
                                MeasurementItem item = new MeasurementItem(object);
                                mMeasurementItems.add(item);
                            }
                        } catch (JSONException ignored) {
                        }

                        mItemAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        mRequestQueue.add(measurementRequest);
    }

    /* Changes ListView's content based on user's mode selection. */
    void repopulateList(int measurementMode) {
        mPartialMeasurementItems.clear();
        for (int i = 0; i < mMeasurementItems.size(); i++) {
            if (mMeasurementItems.get(i).getJenis() == measurementMode) {
                mPartialMeasurementItems.add(mMeasurementItems.get(i));
            }
        }
        mItemAdapter = new ItemAdapter(this, mPartialMeasurementItems);
        mMeasurementListView.setAdapter(mItemAdapter);
        mItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Logbook");
    }
}