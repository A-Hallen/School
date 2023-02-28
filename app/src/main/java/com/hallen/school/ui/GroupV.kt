package com.hallen.school.ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.hallen.school.R


class GroupV(context: Context, groupKey: String? ) : LinearLayout(context) {
    val textView: TextView
    init {
        // Aquí puedes inicializar cualquier cosa que necesites
        // por ejemplo, inflar un layout personalizado
        val view = LayoutInflater.from(context).inflate(R.layout.group_v_layout, this, true)
        textView = view.findViewById(R.id.group_name)
        textView.text = groupKey
    }

    // Aquí puedes agregar cualquier método o propiedad que necesites
    // ...

}