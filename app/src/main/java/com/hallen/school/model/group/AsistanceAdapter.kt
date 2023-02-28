package com.hallen.school.model.group

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hallen.school.R

class AsistanceAdapter(val students: ArrayList<Student>, var date: String): RecyclerView.Adapter<AsistanceAdapter.AsistanceViewHolder>() {

    interface OnItemCheckListener  {  fun onItemCheck (position: Int, checkBox: CheckBox,  isChecked: Boolean)   }
    interface OnChangeNoteListener {  fun onChangeNote(position: Int, button:   ImageView, textView:  TextView)  }
    private lateinit var cbListener: OnItemCheckListener
    private lateinit var nListener: OnChangeNoteListener
    fun setOnChangeNoteListener(nlistener: OnChangeNoteListener){ nListener  = nlistener  }
    fun setOnItemCheckListener (cblistener: OnItemCheckListener){ cbListener = cblistener }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsistanceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.student_item_asistance, parent, false)
        return AsistanceViewHolder(view, cbListener, nListener)
    }

    override fun onBindViewHolder(holder: AsistanceViewHolder, position: Int) {
        val currentItem = students[position]
        holder.nameTextView.text = currentItem.name
        val dateObject = currentItem.asistance[date]
        if (dateObject != null){
            holder.checkBox.isChecked = (dateObject["present"] ?: false) as Boolean
            val nota = (dateObject["nota"]) as Long?
            if (nota == null) {
                holder.notaImageView.visibility = View.VISIBLE
                holder.notaTextView.visibility = View.GONE
            } else {
                holder.notaImageView.visibility = View.GONE
                holder.notaTextView.visibility = View.VISIBLE
                holder.notaTextView.text = nota.toString()
            }
        }
        if (currentItem.index != null) holder.indexText.text = currentItem.index.toString()
    }

    override fun getItemCount(): Int = students.size


    fun addData(student: Student) {
        students.add(student)
        notifyItemInserted(students.size - 1)
    }

    inner class AsistanceViewHolder(view: View, cblistener: OnItemCheckListener, nListener: OnChangeNoteListener): RecyclerView.ViewHolder(view) {
        val indexText:              TextView  = view.findViewById(R.id.index_edit)
        val nameTextView:           TextView  = view.findViewById(R.id.name)
        val checkBox:               CheckBox  = view.findViewById(R.id.checkbox)
        val notaImageView:  ImageView = view.findViewById(R.id.nota_image_view)
        val notaTextView:   TextView  = view.findViewById(R.id.noteTextView)

        init {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                cblistener.onItemCheck(adapterPosition, checkBox, isChecked)
            }
            notaTextView.setOnClickListener {
                nListener.onChangeNote(adapterPosition, notaImageView, notaTextView)
            }
            notaImageView.setOnClickListener {
                nListener.onChangeNote(adapterPosition, notaImageView, notaTextView)
            }
            nameTextView.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                override fun afterTextChanged(s: Editable?) {
                    students[adapterPosition].index = adapterPosition + 1
                }
            })
        }
    }
}