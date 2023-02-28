package com.hallen.school.model.group

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.orhanobut.logger.Logger
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class LineChartXAxisFormater: IndexAxisValueFormatter() {
    var originalTimestamp: Long = 0L
    private val dateFormat: DateFormat = SimpleDateFormat("dd/MMM")

    override fun getFormattedValue(value: Float): String {
        val date = Date(value.toLong() + originalTimestamp)
        Logger.i(dateFormat.format(date) +  " | " + dateFormat.format(originalTimestamp) + " | " + originalTimestamp)
        return dateFormat.format(date)
    }
}