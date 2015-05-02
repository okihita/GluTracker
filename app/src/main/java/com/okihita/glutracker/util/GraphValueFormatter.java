package com.okihita.glutracker.util;

import com.github.mikephil.charting.utils.ValueFormatter;

public class GraphValueFormatter implements ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        return String.valueOf((int) value);
    }
}