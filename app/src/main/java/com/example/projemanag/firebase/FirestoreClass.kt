package com.example.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanag.activities.*
import com.example.projemanag.models.Board
import com.example.projemanag.models.Task
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity:SignUpActivity,userInfo: User){
        mFireStore.collection(Constants.USERS)
            .document(getUserID()).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisterSuccess()
            }
            .addOnFailureListener {
                e->
                Log.e(activity.javaClass.simpleName,"Error writing document",e)
            }
    }

    fun createBoard(activity: CreateBoardActivity,board:Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"Board created successfully")
                Toast.makeText(activity,"Board created successfully",Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener{
                e->
                Log.e(activity.javaClass.simpleName,"Error while creating board",e)
            }
    }

    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO,getUserID())
            .get()
            .addOnSuccessListener {
                document->
                val boardsList :ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)
                    board!!.documentId = i.id
                    boardsList.add(board)
                }
                activity.populateBoardsListToUI(boardsList)
            }.addOnFailureListener {
                e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while loading board: ",e)
            }
    }

    fun updateUserProfileData(activity: Activity,userHashMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity,"User data updated successfuly",Toast.LENGTH_SHORT).show()
                when (activity) {
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }
            }.addOnFailureListener {
                e->
                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Toast.makeText(activity,"Error in updating the profile",Toast.LENGTH_SHORT).show()
            }
    }

    fun loadUserData(activity: Activity,readBoardsList:Boolean = false){
        mFireStore.collection(Constants.USERS)
            .document(getUserID())
            .get()
            .addOnSuccessListener {document->
                Toast.makeText(activity,"Sign in success",Toast.LENGTH_SHORT).show()
                val loggedInUser = document.toObject(User::class.java)!!
                when(activity){
                    is SignInActivity ->{
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity ->{
                        activity.updateNavigationUserDetails(loggedInUser,readBoardsList)
                    }
                    is MyProfileActivity->{
                        activity.setUserDataInUI(loggedInUser)
                    }
                }

            }
            .addOnFailureListener {
                    e->
                when(activity){
                    is SignInActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName,"Error writing document",e)
            }
    }
    fun getUserID():String{
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if(currentUser!=null){
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun addUpdateTaskList(activity: Any,board:Board){
        val taskListHashMap = HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Task updated successfully")
                if(activity is TaskListActivity)
                activity.addUpdateTaskListSuccess()
                if(activity is CardDetailsActivity)
                activity.addUpdateTaskListSuccess()
            }.addOnFailureListener {
                e->
                if(activity is TaskListActivity)
                activity.hideProgressDialog()
                if(activity is CardDetailsActivity)
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error in updating task")
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentID: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentID)
            .get()
            .addOnSuccessListener {
                    document->
               Log.i(activity.javaClass.simpleName,document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }.addOnFailureListener {
                    e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while loading board: ",e)
            }
    }

    fun getAssignedMembersArrayList(activity:Activity,assignedTo:ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID,assignedTo)
            .get()
            .addOnSuccessListener {
                document->
                Log.e(activity.javaClass.simpleName,document.documents.toString())
                val userList:ArrayList<User> = ArrayList()

                for(i in document){
                    userList.add(i.toObject(User::class.java)!!)
                }
                if(activity is MembersActivity)
                    activity.setupMembersList(userList)
                if(activity is TaskListActivity)
                    activity.boardMemberDetails(userList)
            }.addOnFailureListener {
                e->
                if(activity is MembersActivity)
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while getting members",e)
            }
    }

    fun getUserWithEmailId(activity:MembersActivity
                           ,email: String) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.email,email)
            .get()
            .addOnSuccessListener {
                document->
                Log.e("USER WITH EMAIL ",document.documents.toString())
                if(document.size()>0){
                    val user:User = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("Entered email id is not registered")
                }
            }.addOnFailureListener {
                    e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while getting members",e)
            }
    }

    fun assignMemberToBoard(activity: MembersActivity,board:Board,user:User){
        val assignedUserHaspMap = HashMap<String,Any>()
        assignedUserHaspMap[Constants.ASSIGNED_TO] = board.assignedTo
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedUserHaspMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }.addOnFailureListener {
                    e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while updating assignedTo list ",e)
            }
    }



}