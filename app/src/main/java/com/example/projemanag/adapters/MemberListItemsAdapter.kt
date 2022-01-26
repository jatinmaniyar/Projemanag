package com.example.projemanag.adapters

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.models.Card
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.item_card.view.*
import kotlinx.android.synthetic.main.item_member.view.*

open class MemberListItemsAdapter(private val context:Context,private var list : ArrayList<User>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_member,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_placeholder_gray)
                .into(holder.itemView.iv_member_image)
            holder.itemView.tv_member_name.text = model.name
            holder.itemView.tv_member_email.text = model.email

            if(model.selected){
                holder.itemView.iv_selected_member.visibility = View.VISIBLE
            }else{
                holder.itemView.iv_selected_member.visibility = View.GONE
            }

            holder.itemView.setOnClickListener{
                if(model.selected){
                    onClickListener!!.onClick(position,model,Constants.UN_SELECT)
                }else{
                    onClickListener!!.onClick(position,model,Constants.SELECT)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener{
        fun onClick(position:Int,user:User,action:String)
    }

    fun setOnClickListener(onClickListener:OnClickListener){
        this.onClickListener = onClickListener
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}