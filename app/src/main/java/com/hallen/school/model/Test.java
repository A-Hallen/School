package com.hallen.school.model;

import com.github.mikephil.charting.data.BarEntry;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Test {
    public Test(){
        TimeUnit.MILLISECONDS.toDays((long) new Date().getTime());
        new BarEntry(TimeUnit.MILLISECONDS.toDays((long)new Date().getTime()), 0.0f);
    }
}

