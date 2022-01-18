package com.example.projemanag.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.io.IOException
import java.util.jar.Manifest

class MyProfileActivity : BaseActivity() {

    companion object{
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }

    private var mSelectedImageURI: Uri? = null
    private lateinit var mUserDetails : User
    private var mProfileImageUri : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        FirestoreClass().loadUserData(this)
        setupActionBar()
        iv_my_profile.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                showImageChooser()
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        btn_update_profile.setOnClickListener{
            if(mSelectedImageURI!=null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                showImageChooser()
            }else{
                Toast.makeText(this,"Oops, the permission is not granted, you can grant" +
                        "it from the settings",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode== PICK_IMAGE_REQUEST_CODE && data!!.data!=null){
            mSelectedImageURI = data.data
            try{
                Glide
                    .with(this)
                    .load(mSelectedImageURI)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_placeholder_gray)
                    .into(iv_my_profile)
            }catch (e:IOException){
                e.printStackTrace()
            }
        }
    }

    private fun showImageChooser(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if(mSelectedImageURI!=null){
            val sRef:StorageReference = FirebaseStorage.getInstance()
                .reference.child(
                    "USER_IMAGE"+System.currentTimeMillis()+"."+getFileExtension(mSelectedImageURI)
                )
            sRef.putFile(mSelectedImageURI!!).addOnSuccessListener {
                takeSnapshot->
                Log.e("Firebase image URI",takeSnapshot.metadata!!.reference!!.downloadUrl.toString())
                takeSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    Log.i("Downloadable image URI",uri.toString())
                    mProfileImageUri = uri.toString()
                    updateUserProfileData()

                }
            }.addOnFailureListener{
                exception->
                Toast.makeText(this@MyProfileActivity,exception.message,Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(tool_bar_my_profile)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = resources.getString(R.string.my_profile)
        }
        tool_bar_my_profile.setNavigationOnClickListener { onBackPressed() }
    }

    fun updateUserProfileData(){
        val userHashMap = HashMap<String,Any>()
        if(mProfileImageUri!=null && mProfileImageUri!=mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageUri
        }
        if(et_name_my_profile.text.toString()!=mUserDetails.name){
            userHashMap[Constants.NAME] = et_name_my_profile.text.toString()
        }
        if(et_mobile_my_profile.text.toString().toLong()!=mUserDetails.mobile){
            userHashMap[Constants.MOBILE] = et_mobile_my_profile.text.toString().toLong()
        }
        FirestoreClass().updateUserProfileData(this,userHashMap)
    }

    fun setUserDataInUI(user: User) {
        mUserDetails = user
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_placeholder_gray)
            .into(iv_my_profile)

        et_name_my_profile.setText(user.name)
        et_email_my_profile.setText(user.email)

        if(user.mobile!=0L)
            et_mobile_my_profile.setText(user.mobile.toString())
    }

    private fun getFileExtension(uri:Uri?):String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}