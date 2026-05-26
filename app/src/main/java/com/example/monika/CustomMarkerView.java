package com.example.monika;

import android.content.Context;
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import java.util.Locale;

public class CustomMarkerView extends MarkerView {
    private final TextView tvContent;
    private String[] labels;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent);
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String label = (labels != null && (int) e.getX() < labels.length) ? labels[(int) e.getX()] : "";
        tvContent.setText(String.format(Locale.GERMAN, "%s: %.1f%%", label, e.getY()));
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight() - 10);
    }
}
