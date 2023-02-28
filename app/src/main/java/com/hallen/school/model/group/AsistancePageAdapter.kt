package com.hallen.school.model.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.firebase.database.DatabaseReference
import com.hallen.school.ui.groups.AsistenceFragment
import com.hallen.school.ui.welcome.FragmentHorario
import com.hallen.school.ui.welcome.FragmentRegistro

class AsistancePageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val group: DatabaseReference
): FragmentStateAdapter(fragmentManager, lifecycle) {
    init {
        FECHA = null
    }
    companion object {
        var FECHA: String? = null
    }
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val fragmentRegistro = FragmentRegistro(group)
        val grafics  = Bundle()
        val registro = Bundle()
        registro.putString("fragment", "registro")
        grafics.putString("fragment", "grafics")
        return when(position){
            0 -> AsistenceFragment(group, FECHA)
            1 -> fragmentRegistro.also { it.arguments =  registro}
            2 -> fragmentRegistro.also { it.arguments = grafics }
            else -> FragmentHorario()
        }

    }


}