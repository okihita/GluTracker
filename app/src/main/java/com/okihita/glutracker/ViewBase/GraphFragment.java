package com.okihita.glutracker.ViewBase;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class GraphFragment extends Fragment {

    private RequestQueue mRequestQueue;
    private BarChart mChartPremeal;
    private BarChart mChartPostmeal;
    private BarChart mChartRandom;
    private ArrayList<MeasurementItem> mMeasurementItems = new ArrayList<>();

    private SimpleDateFormat sdf = (new SimpleDateFormat("dd MMM", new Locale("id", "ID")));

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

    private void specificChartSet(int measurementMode, BarChart chart, String label) {

        /* Make an array list containing ONLY data from a mode. */
        ArrayList<MeasurementItem> tempMeasurementItems = new ArrayList<>();
        String tempDate = sdf.format(mMeasurementItems.get(0).getTanggalAmbil());

        for (int i = 0; i < mMeasurementItems.size(); i++) {
            String nowDate = sdf.format(mMeasurementItems.get(i).getTanggalAmbil());
            if (!nowDate.equals(tempDate) && mMeasurementItems.get(i).getJenis() == measurementMode) {
                tempMeasurementItems.add(mMeasurementItems.get(i));
                tempDate = nowDate;
            }
        }

        /* Set the y-axis and x-axis values. */
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<BarEntry> yVals = new ArrayList<>();

        for (int i = 0; i < tempMeasurementItems.size(); i++) {
            xVals.add(sdf.format(tempMeasurementItems.get(i).getTanggalAmbil()));
            yVals.add(new BarEntry(tempMeasurementItems.get(i).getKadar(), i));
        }

        /* Initiate, hook, and refresh the datasets. */
        BarDataSet dataSet = new BarDataSet(yVals, label);
        dataSet.setBarSpacePercent(35f);
        dataSet.setValueFormatter(new GraphValueFormatter());

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        BarData barData = new BarData(xVals, dataSets);
        barData.setValueTextSize(10f);
        chart.setData(barData);

        chart.invalidate();
    }

    private void setChartData() {
        specificChartSet(Config.MEASUREMENT_MODE_PREMEAL, mChartPremeal, "Premeal Chart");
        specificChartSet(Config.MEASUREMENT_MODE_POSTMEAL, mChartPostmeal, "Postmeal Chart");
        specificChartSet(Config.MEASUREMENT_MODE_RANDOM, mChartRandom, "Random Chart");
    }

    /**
     * Contacting server to receive items.
     */
    private void fetchMeasurementItems() {

        Uri.Builder mBaseUriBuilder = (new Uri.Builder()).scheme("http")
                .authority(Config.BASE_URL)
                .appendPath(Config.SUBDOMAIN_ADDRESS);
        String graphMeasurementDataQuery = mBaseUriBuilder.build().toString();

        JsonArrayRequest measurementRequest = new JsonArrayRequest(
                graphMeasurementDataQuery,
                new Response.Listener<JSONArray>() {
                   @Override
                    public void onResponse(JSONArray response) {
                        try {
                            mMeasurementItems.clear();
                            for (int i = 0; i < response.length(); i++)
                                mMeasurementItems.add(new MeasurementItem(response.getJSONObject(i)));
                            setChartData();
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