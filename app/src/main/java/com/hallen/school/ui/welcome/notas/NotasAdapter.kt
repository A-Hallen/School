package com.hallen.school.ui.welcome.notas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.hallen.school.R

class NotasAdapter(private val context: Context, private val notasArray: ArrayList<Pair<String, Nota>>) : RecyclerView.Adapter<NotasAdapter.NotasViewHolder>() {

    private lateinit var itemClickListener: OnItemClickListenr
    private lateinit var itemLongClickListener: OnItemLongClickListener

    interface OnItemLongClickListener {   fun onItemLongClick(
        mapNota: Pair<String, Nota>,
        adapterPosition: Int,
        view: View
    )}
    interface OnItemClickListenr      {   fun onItemClick(item: Pair<String, Nota>, adapterPosition: Int)  }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener){ itemLongClickListener = listener}
    fun setOnItemClickListener(listener: OnItemClickListenr){   itemClickListener = listener   }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotasViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.notas_item, parent, false)
        return NotasViewHolder(view, itemClickListener, itemLongClickListener)
    }

    override fun onBindViewHolder(holder: NotasViewHolder, position: Int) {
        val currentItem = notasArray[position].second


        // Asign data to view
        holder.titleView.text   = currentItem.title
        holder.contentView.text = currentItem.content
    }

    override fun getItemCount(): Int = notasArray.size

    fun updateNota(nota: Pair<String, Nota>){
        notasArray[nota.second.index] = nota
        notifyItemChanged(nota.second.index)
    }

    fun addNota(nota: Pair<String, Nota>) {
        notasArray.add(nota.second.index, nota)
        notifyItemInserted(nota.second.index)
    }

    fun setNotas(notas: ArrayList<Pair<String, Nota>>) {
        notasArray.clear()
        notasArray.addAll(notas)
        notifyDataSetChanged()
    }

    fun getSize(): Int =  notasArray.size

    fun getItemKey(actualItemPosition: Int): String = notasArray[actualItemPosition].first

    fun deleteItem(adapterPosition: Int, notas: ArrayList<Pair<String, Nota>>) {
        notasArray.clear()
        notasArray.addAll(notas)
        notifyItemRemoved(adapterPosition)
        Toast.makeText(context, "Item removido en la posicion $adapterPosition", Toast.LENGTH_SHORT).show()
    }

    inner class NotasViewHolder(
        view: View,
        itemClickListener: OnItemClickListenr,
        itemLongClickListener: OnItemLongClickListener
    ): RecyclerView.ViewHolder(view) {
        val titleView:   TextView = view.findViewById(R.id.nota_title)
        val contentView: TextView = view.findViewById(R.id.nota_content)

        init {
            view.setOnClickListener{
                itemClickListener.onItemClick(notasArray[adapterPosition], adapterPosition)
            }
            view.setOnLongClickListener {
                itemLongClickListener.onItemLongClick(notasArray[adapterPosition], adapterPosition, it)
                true
            }
        }
    }
}
