package com.okihita.glutracker.model;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.okihita.glutracker.R;
import com.okihita.glutracker.ViewBase.LogbookFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ItemAdapter extends ArrayAdapter<MeasurementItem> {

    private LogbookFragment mLogbookFragment;

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
        TextView date = (TextView) convertView.findViewById(R.id.meaitem_TextView_date);

        String s = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("id", "ID"))).format(mi.getTanggalAmbil());
        date.setText(s);
        TextView kadar = (TextView) convertView.findViewById(R.id.meaitem_TextView_kadar);
        kadar.setText(String.valueOf(mi.getKadar()) + " mg/dL");
        TextView jenis = (TextView) convertView.findViewById(R.id.meaitem_TextView_jenis);
        jenis.setText(String.valueOf(mi.getJenisTeks()));
        return convertView;
    }
}
