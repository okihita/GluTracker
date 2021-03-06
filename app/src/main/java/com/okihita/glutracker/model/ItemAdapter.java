package com.okihita.glutracker.model;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.okihita.glutracker.R;
import com.okihita.glutracker.ViewBase.LogbookFragment;
import com.okihita.glutracker.util.Config;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ItemAdapter extends ArrayAdapter<MeasurementItem> {

    private final LogbookFragment mLogbookFragment;

    public ItemAdapter(LogbookFragment logbookFragment, ArrayList<MeasurementItem> items) {
        super(logbookFragment.getActivity(), 0, items);
        mLogbookFragment = logbookFragment;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // If we weren't given a view, inflate one.
        if (convertView == null)
            convertView = mLogbookFragment.getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_measurement, null);

        // Configure the view for this item.
        MeasurementItem mi = getItem(position);
        TextView date = (TextView) convertView.findViewById(R.id.measurementItem_TextView_date);

        String s = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("id", "ID"))).format(mi.getTanggalAmbil());
        date.setText(s);

        TextView level = (TextView) convertView.findViewById(R.id.measurementItem_TextView_level);

        int sugarlevel = Config.bloodSugarLevel(mLogbookFragment.getActivity().getApplicationContext(), mi.getJenis(), mi.getKadar());

        level.setText(sugarlevel == 1 ? "Low" : sugarlevel == 2 ? "Normal" : "High");
        TextView kadar = (TextView) convertView.findViewById(R.id.measurementItem_TextView_kadar);
        kadar.setText(String.valueOf(mi.getKadar()) + " mg/dL");
        TextView jenis = (TextView) convertView.findViewById(R.id.measurementItem_TextView_jenis);
        jenis.setText(String.valueOf(mi.getJenisTeks()));
        return convertView;
    }
}
