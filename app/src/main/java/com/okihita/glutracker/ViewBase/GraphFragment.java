package com.okihita.glutracker.ViewBase;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.okihita.glutracker.R;
import com.okihita.glutracker.model.MeasurementItem;
import com.okihita.glutracker.util.Config;
import com.okihita.glutracker.util.GraphValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class GraphFragment extends Fragment {

    private RequestQueue mRequestQueue;
    private BarChart mChartPremeal;
    private BarChart mChartPostmeal;
    private BarChart mChartRandom;
    private ArrayList<MeasurementItem> mMeasurementItems = new ArrayList<>();
    private ArrayList<MeasurementItem> mUniqueDatePREMEALMeasurementItems = new ArrayList<>();
    private ArrayList<MeasurementItem> mUniqueDatePOSTMEALMeasurementItems = new ArrayList<>();
    private ArrayList<MeasurementItem> mUniqueDateRANDOMMeasurementItems = new ArrayList<>();

    private SimpleDateFormat mysdf = (new SimpleDateFormat("dd MMMM", new Locale("id", "ID")));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(getActivity());
        fetchMeasurementItems();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        mChartPremeal = (BarChart) view.findViewById(R.id.bar_chart_premeal);
        mChartPostmeal = (BarChart) view.findViewById(R.id.bar_chart_postmeal);
        mChartRandom = (BarChart) view.findViewById(R.id.bar_chart_random);

        /* Basic chart settings. s*/
        prepareChart(mChartPremeal);
        prepareChart(mChartPostmeal);
        prepareChart(mChartRandom);

        return view;
    }

    private void prepareChart(BarChart chart) {
        chart.setDrawValueAboveBar(true);
        chart.getAxisLeft().setValueFormatter(new GraphValueFormatter());
        chart.getAxisRight().setEnabled(false);
    }

    private void setChartData() {
        /* TIAP INDIVIDU */
        // PREMEAL
        String tanggalTempPremeal = (mysdf).format(mMeasurementItems.get(0).getTanggalAmbil());
        for (int i = 0; i < mMeasurementItems.size(); i++) {
            String tanggalSekarang = (mysdf).format(mMeasurementItems.get(i).getTanggalAmbil());
            if (!tanggalSekarang.equals(tanggalTempPremeal) && mMeasurementItems.get(i).getJenis() == Config.MEASUREMENT_MODE_PREMEAL) {
                mUniqueDatePREMEALMeasurementItems.add(mMeasurementItems.get(i));
                tanggalTempPremeal = tanggalSekarang;
                Log.d(Config.TAG, "Tanggal " + tanggalSekarang + " dengan tipe " + mMeasurementItems.get(i).getJenisTeks()
                        + " masuk. Nilai: " + mMeasurementItems.get(i).getKadar());
            }
        }

        // Initiating X-values and Y-Values for premeal
        ArrayList<String> xValsPremeal = new ArrayList<>();
        ArrayList<BarEntry> yValsPremeal = new ArrayList<>();
        for (int i = 0; i < mUniqueDatePREMEALMeasurementItems.size(); i++) {
            Log.d(Config.TAG, "Nomor " + i + " kadarnya: " + mUniqueDatePREMEALMeasurementItems.get(i).getKadar());
            xValsPremeal.add((new SimpleDateFormat("dd MMMM", Locale.ENGLISH)).format(mUniqueDatePREMEALMeasurementItems.get(i).getTanggalAmbil()));
            yValsPremeal.add(new BarEntry(mUniqueDatePREMEALMeasurementItems.get(i).getKadar(), i));
        }

        /* Buat sebuah set: PREMEAL. */
        BarDataSet datasetPremeal = new BarDataSet(yValsPremeal, "Premeal Result");
        datasetPremeal.setBarSpacePercent(35f);
        datasetPremeal.setValueFormatter(new GraphValueFormatter());
        ArrayList<BarDataSet> dataSetsPremeal = new ArrayList<>();
        dataSetsPremeal.add(datasetPremeal);
        BarData premealBarData = new BarData(xValsPremeal, dataSetsPremeal);
        premealBarData.setValueTextSize(10f);

        mChartPremeal.setData(premealBarData);


        // POSTMEAL
        String tanggalTempPostmeal = (mysdf).format(mMeasurementItems.get(0).getTanggalAmbil());
        for (int i = 0; i < mMeasurementItems.size(); i++) {
            String tanggalSekarang = (mysdf).format(mMeasurementItems.get(i).getTanggalAmbil());
            if (!tanggalSekarang.equals(tanggalTempPostmeal) && mMeasurementItems.get(i).getJenis() == Config.MEASUREMENT_MODE_POSTMEAL) {
                mUniqueDatePOSTMEALMeasurementItems.add(mMeasurementItems.get(i));
                tanggalTempPostmeal = tanggalSekarang;
                Log.d(Config.TAG, "Tanggal " + tanggalSekarang + " dengan tipe " + mMeasurementItems.get(i).getJenisTeks()
                        + " masuk. Nilai: " + mMeasurementItems.get(i).getKadar());
            }
        }

        // Initiating X-values and Y-Values for postmeal
        ArrayList<String> xValsPostmeal = new ArrayList<>();
        ArrayList<BarEntry> yValsPostmeal = new ArrayList<>();
        for (int i = 0; i < mUniqueDatePOSTMEALMeasurementItems.size(); i++) {
            Log.d(Config.TAG, "Nomor " + i + " kadarnya: " + mUniqueDatePOSTMEALMeasurementItems.get(i).getKadar());
            xValsPostmeal.add((mysdf).format(mUniqueDatePOSTMEALMeasurementItems.get(i).getTanggalAmbil()));
            yValsPostmeal.add(new BarEntry(mUniqueDatePOSTMEALMeasurementItems.get(i).getKadar(), i));
        }


        // RANDOM
        String tanggalTempRandom = (mysdf).format(mMeasurementItems.get(0).getTanggalAmbil());
        for (int i = 0; i < mMeasurementItems.size(); i++) {
            String tanggalSekarang = (mysdf).format(mMeasurementItems.get(i).getTanggalAmbil());
            if (!tanggalSekarang.equals(tanggalTempRandom) && mMeasurementItems.get(i).getJenis() == Config.MEASUREMENT_MODE_RANDOM) {
                mUniqueDateRANDOMMeasurementItems.add(mMeasurementItems.get(i));
                tanggalTempRandom = tanggalSekarang;
                Log.d(Config.TAG, "Tanggal " + tanggalSekarang + " dengan tipe " + mMeasurementItems.get(i).getJenisTeks()
                        + " masuk. Nilai: " + mMeasurementItems.get(i).getKadar());
            }
        }

        // Initiating X-values and Y-Values for random
        ArrayList<String> xValsRandom = new ArrayList<>();
        ArrayList<BarEntry> yValsRandom = new ArrayList<>();
        for (int i = 0; i < mUniqueDateRANDOMMeasurementItems.size(); i++) {
            Log.d(Config.TAG, "Nomor " + i + " kadarnya: " + mUniqueDateRANDOMMeasurementItems.get(i).getKadar());
            xValsRandom.add((mysdf).format(mUniqueDateRANDOMMeasurementItems.get(i).getTanggalAmbil()));
            yValsRandom.add(new BarEntry(mUniqueDateRANDOMMeasurementItems.get(i).getKadar(), i));
        }


        /* Buat sebuah set: POSTMEAL. */
        BarDataSet datasetPostmeal = new BarDataSet(yValsPostmeal, "Postmeal Result");
        datasetPostmeal.setBarSpacePercent(35f);
        datasetPostmeal.setValueFormatter(new GraphValueFormatter());
        ArrayList<BarDataSet> dataSetsPostmeal = new ArrayList<>();
        dataSetsPostmeal.add(datasetPostmeal);
        BarData postmealBarData = new BarData(xValsPostmeal, dataSetsPostmeal);
        postmealBarData.setValueTextSize(10f);

        /* Buat sebuah set: RANDOM. */
        BarDataSet datasetRandom = new BarDataSet(yValsRandom, "Random Result");
        datasetRandom.setBarSpacePercent(35f);
        datasetRandom.setValueFormatter(new GraphValueFormatter());
        ArrayList<BarDataSet> dataSetsRandom = new ArrayList<>();
        dataSetsRandom.add(datasetRandom);
        BarData randomBarData = new BarData(xValsRandom, dataSetsRandom);
        randomBarData.setValueTextSize(10f);

        /* Attach datasets to charts. */
        mChartPostmeal.setData(postmealBarData);
        mChartRandom.setData(randomBarData);
    }

    /**
     * Contacting server to receive items.
     */
    private void fetchMeasurementItems() {

        Uri.Builder mBaseUriBuilder = (new Uri.Builder()).scheme("http")
                .authority(Config.BASE_URL)
                .appendPath(Config.SUBDOMAIN_ADDRESS);
        String graphFragmentQuery = mBaseUriBuilder.build().toString();

        JsonArrayRequest measurementRequest = new JsonArrayRequest(
                graphFragmentQuery,
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

                            setChartData();
                            mChartPremeal.invalidate();
                            mChartPostmeal.invalidate();
                            mChartRandom.invalidate();
                        } catch (JSONException ignored) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        mRequestQueue.add(measurementRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Graph");
    }
}
