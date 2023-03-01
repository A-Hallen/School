package com.hallen.school.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.github.mikephil.charting.data.BarEntry;
import com.hallen.school.R;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Test {
    public Test(){
        TimeUnit.MILLISECONDS.toDays((long) new Date().getTime());
        new BarEntry(TimeUnit.MILLISECONDS.toDays((long)new Date().getTime()), 0.0f);
    }
}

class AdapterTest extends BaseAdapter{

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(view.getContext()).inflate(R.layout.event_list_item, viewGroup, false);
        return view;
    }
}

