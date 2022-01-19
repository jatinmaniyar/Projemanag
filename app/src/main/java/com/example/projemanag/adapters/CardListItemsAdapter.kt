package com.example.projemanag.adapters

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.models.Card
import kotlinx.android.synthetic.main.item_card.view.*

open class CardListItemsAdapter(private val context:Context,private var list : ArrayList<Card>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_card,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            holder.itemView.tv_card_name.text = model.name
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnCLickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int,card: Card)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}