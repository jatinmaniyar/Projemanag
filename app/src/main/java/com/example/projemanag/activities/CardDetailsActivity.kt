package com.example.projemanag.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.TaskListItemsAdapter
import com.example.projemanag.dialogs.LabelColorListDialog
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.Card
import com.example.projemanag.models.Task
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.projemanag.adapters.CardMemberListItemsAdapter
import com.projemanag.dialogs.MembersListDialog
import com.projemanag.model.SelectedMembers
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_task_list.*

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails : Board
    private lateinit var mMembersDetailList : ArrayList<User>
    private var mTaskListPosition = -1
    private var mCardListPosition = -1
    private var mSelectedColor = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        initializeValueFromIntent()
        setupActionBar()
        et_name_card_details.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length)
        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        btn_update_card_details.setOnClickListener{
            if(et_name_card_details.text.toString().isNotEmpty()){
                updateCardDetails()
            }else{
                Toast.makeText(this,"Please enter a card name",Toast.LENGTH_SHORT).show()
            }
        }
        tv_select_label_color.setOnClickListener {
            labelColorListDialog()
        }

        tv_select_members.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_card_details_activity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
        }
        toolbar_card_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card ->{
                deleteAlertDialog(et_name_card_details.text.toString())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun colorsList():ArrayList<String>{
        val colorList : ArrayList<String> = ArrayList()
        colorList.add("#f3c86f")
        colorList.add("#0c90f1")
        colorList.add("#f72400")
        colorList.add("#770000")
        return colorList
    }

    private fun setColor(){
        tv_select_label_color.text = ""
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun labelColorListDialog(){
        val colorList : ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(
            this,
            colorList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun deleteAlertDialog(title:String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title?")
            .setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){dialogInterface,which->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton("No"){dialogInterface,which->
            dialogInterface.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun deleteCard(){
        val cardList : ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardList.removeAt(mCardListPosition)
        var taskList : ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size -1)
        taskList[mTaskListPosition].cards = cardList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)
    }

    private fun initializeValueFromIntent(){
        if(intent.hasExtra(Constants.BOARDS)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARDS)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardListPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList = intent.getParcelableArrayListExtra<User>(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun updateCardDetails(){
        val card = Card(et_name_card_details.text.toString(),
        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo,
            mSelectedColor
        )
        val taskList :ArrayList<Task> =mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)
        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)

    }

    private fun membersListDialog(){
        var cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo

        if(cardAssignedMembersList.size>0){
            for(i in mMembersDetailList.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailList[i].id==j){
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        }else{
            for(i in mMembersDetailList.indices){
                mMembersDetailList[i].selected = false
            }
        }
        val listDialog = object : MembersListDialog(
            this,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)
        ){
            override fun onItemSelected(user: User, action: String) {
                if(action == Constants.SELECT){
                    if(!mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
                            .contains(user.id)){

                        mBoardDetails.taskList[mTaskListPosition]
                            .cards[mCardListPosition].assignedTo.add(user.id)
                    }
                }else{
                    mBoardDetails.taskList[mTaskListPosition]
                        .cards[mCardListPosition].assignedTo.remove(user.id)

                    for(i in mMembersDetailList.indices){
                        if(mMembersDetailList[i].id == user.id){
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(RESULT_OK)
        finish()
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMembersDetailList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )

                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {

            selectedMembersList.add(SelectedMembers("", ""))

            tv_select_members.visibility = View.GONE
            rv_selected_members_list.visibility = View.VISIBLE

            rv_selected_members_list.layoutManager = GridLayoutManager(this@CardDetailsActivity, 6)
            val adapter = CardMemberListItemsAdapter(this@CardDetailsActivity, selectedMembersList)
            rv_selected_members_list.adapter = adapter
            adapter.setOnClickListener(object :
                CardMemberListItemsAdapter.OnClickListener {
                override fun onClick() {
                    membersListDialog()
                }
            })
        } else {
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility = View.GONE
        }
    }
}