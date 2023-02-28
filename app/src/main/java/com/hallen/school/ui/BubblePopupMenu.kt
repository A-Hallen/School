package com.hallen.school.ui

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.hallen.school.R

class BubblePopupMenu(
    private val context: Context,
    private val anchorView: View,
    private val onClickListener: PopupMenuCustomOnClickListener
) {
    private val popupWindow: PopupWindow
    private val popupView: View

    fun setAnimationStyle(animationStyle: Int) {
        popupWindow.animationStyle = animationStyle
    }

    fun show(){
        popupWindow.showAsDropDown(anchorView)
    }

    interface PopupMenuCustomOnClickListener {
        fun onClick(index: Int, view: View)
    }

    init {
        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.bubble_menu_design,null, false)
        val container: LinearLayout = popupView.findViewById(R.id.bubble_container)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.elevation = 10f
        for (i in 0 until container.childCount) {
            val v: View = container.getChildAt(i)
            v.setOnClickListener { v1 ->
                onClickListener.onClick(i, v1)
                popupWindow.dismiss()
            }
        }
    }

}