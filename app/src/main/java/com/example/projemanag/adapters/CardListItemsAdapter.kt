package com.example.projemanag.adapters

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.models.Card
import com.projemanag.adapters.CardMemberListItemsAdapter
import com.projemanag.model.SelectedMembers
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
            if(model.labelColor.isNotEmpty()){
                holder.itemView.view_label_color.visibility = View.VISIBLE
                holder.itemView.view_label_color.setBackgroundColor(Color.parseColor(model.labelColor))
            }else{
                holder.itemView.view_label_color.visibility = View.GONE
            }
            holder.itemView.tv_card_name.text = model.name
            if((context as TaskListActivity).mAssignedMembersList.size>0){
                val selectedMemberList : ArrayList<SelectedMembers> =ArrayList()
                for(i in context.mAssignedMembersList.indices){
                    for(j in model.assignedTo){
                        if(context.mAssignedMembersList[i].id == j){
                            val selectedMembers = SelectedMembers(
                                context.mAssignedMembersList[i].id,
                                context.mAssignedMembersList[i].image
                            )
                            selectedMemberList.add(selectedMembers)
                        }
                    }
                }
                if(selectedMemberList.size>0){
                    if(selectedMemberList.size==0 && selectedMemberList[0].id==model.createdBy){
                        holder.itemView.rv_card_selected_members_list.visibility = View.GONE
                    }else{
                        holder.itemView.rv_card_selected_members_list.visibility = View.VISIBLE
                        holder.itemView.rv_card_selected_members_list.layoutManager =
                            GridLayoutManager(context,4)
                        val adapter = CardMemberListItemsAdapter(context,selectedMemberList,false)
                        holder.itemView.rv_card_selected_members_list.adapter = adapter
                        adapter.setOnClickListener(
                            object : CardMemberListItemsAdapter.OnClickListener{
                                override fun onClick() {
                                    if(onClickListener!=null){
                                        onClickListener!!.onClick(position)
                                    }
                                }
                            })
                    }
                }else{
                    holder.itemView.rv_card_selected_members_list.visibility = View.GONE
                }
            }
            holder.itemView.setOnClickListener {
                if(onClickListener!=null){
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnCLickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}