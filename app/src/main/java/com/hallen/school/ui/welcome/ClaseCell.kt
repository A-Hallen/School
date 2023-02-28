package com.hallen.school.ui.welcome

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hallen.school.R

class ClaseCell(context: Context): LinearLayout(context) {
    private lateinit var view: View
    val grupo by lazy<TextView> {   view.findViewById(R.id.grupo)    }
    val local: TextView by lazy {   view.findViewById(R.id.local)    }

    fun setTextColor(color: Int){
        grupo.setTextColor(color)
        local.setTextColor(color)
    }
    init {
        view = LayoutInflater.from(context).inflate(R.layout.clase_row, this, true)
    }
}

class HeaderCell(context: Context): androidx.appcompat.widget.AppCompatTextView(context){
    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.steel_blue))
        setTextColor(Color.WHITE)
        setPadding(0, 10.pxToDp(context), 0, 10.pxToDp(context))
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
    }
}

private fun Int.pxToDp(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}
