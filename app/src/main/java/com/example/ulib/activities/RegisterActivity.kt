package com.example.ulib.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.ulib.R
import com.example.ulib.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    companion object
    {
        lateinit var auth: FirebaseAuth

    }
    private lateinit var binding:ActivityRegisterBinding
    private lateinit var mProgressDialog: Dialog
    //buttons
    private var bRegister: Button?=null
    //edit text
    private var etFirstName: EditText?=null
    private var etLastName: EditText?=null
    var etEmail: EditText?=null
    var etPassword: EditText?=null
    var etConfPassword: EditText?=null
    var cbTermConditions: CheckBox?=null
    var backbtn: ImageView?=null
    var isAllEditTextCheck=false
    var name=""
    var email=""
    var password=""

    private lateinit var textlogin : TextView
    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        etEmail=binding.emailEt
        etFirstName=binding.firstName
        etLastName=binding.lastName
        etPassword=binding.passwordEt
        etConfPassword=binding.confPasswordEt
        cbTermConditions=binding.termsCondCbox
        bRegister=binding.registerBtn
        backbtn=binding.backImage

        backbtn!!.setOnClickListener{
            val intent= Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        bRegister?.setOnClickListener { registerUser() }

        textlogin = findViewById(R.id.login)
        textlogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
    private fun registerUser()
    {
        email= etEmail?.text.toString().trim()
        password=etPassword?.text.toString().trim()
        name=etFirstName?.text.toString().trim()+" " +etLastName?.text.toString().trim()
        isAllEditTextCheck=checkAllEditText()
        if(isAllEditTextCheck){
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener {
                    updateUserInfo()
                }
                .addOnFailureListener{
                    hideProgressDialog()
                    Toast.makeText(this,"Failed creating account",Toast.LENGTH_SHORT).show()
                }
        }
        else
        {
            Toast.makeText(this,"Registration is not successful", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isEmail(text: Editable?): Boolean {
        val email: CharSequence = text.toString()
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun checkAllEditText(): Boolean {
        if(etFirstName?.length()  ==0)
        {
            etFirstName?.error = "First Name cannot be blank"
            return false;
        }
        if(etLastName?.length()==0)
        {
            etLastName?.error="Last Name cannot be blank"
            return false;
        }
        if(etEmail?.length()==0)
        {
            etEmail?.error="Email ID cannot be blank"
            return false;
        }
        if(!isEmail(etEmail?.editableText))
        {
            etEmail?.error="The email address is invalid"
            return false
        }
        if (etPassword!!.length() == 0) {
            etPassword!!.error = "Password cannot be blank"
            return false
        } else if (etPassword!!.length() < 8) {
            etPassword!!.error = "Password must be of minimum 8 characters"
            return false
        }
        if(etConfPassword!!.length()==0)
        {
            etConfPassword!!.error="Confirm your password first."
            return false
        }
        if(!etConfPassword?.equals(etConfPassword)!!)
        {
            etConfPassword!!.error="The passwords do not match."
            return false
        }
        if (cbTermConditions?.isChecked == true) {
            (cbTermConditions!!.text.toString() + " ")
        } else {
            (cbTermConditions!!.text.toString() + "UnChecked")
            Toast.makeText(this@RegisterActivity,"Check the terms and conditions", Toast.LENGTH_SHORT).show()
            return false
        }
        return true

    }
    private fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        mProgressDialog.setContentView(R.layout.dialog_progress)

        mProgressDialog.findViewById<TextView>(R.id.tv_progress_text).text = text

        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)

        //Start the dialog and display it on screen.
        mProgressDialog.show()
    }

    private fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }
    private  fun updateUserInfo(){
//        save user info in firebase realtime database
        showProgressDialog(resources.getString(R.string.Saving_User_Info))
        val timestamp=System.currentTimeMillis()
        val uid= auth.uid
        val hashMap:HashMap<String,Any?> = HashMap()
        hashMap["uid"]=uid
        hashMap["email"]=email
        hashMap["name"]=name
        hashMap["profileImage"]=" "//add empty,will do in profile edit
        hashMap["userType"]="user"
        hashMap["timestamp"]=timestamp
//        set data to db
        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
//                user info saved dashboard open
                hideProgressDialog()
                Toast.makeText(this,"Account Created Successfully",Toast.LENGTH_SHORT).show()
                val intent=Intent(this@RegisterActivity, DashBoardUserActivity::class.java)
                startActivity(intent)
                finish()

            }
            .addOnFailureListener{
//                failed adding data to db
                hideProgressDialog()
                Toast.makeText(this,"Failed saving Users info ",Toast.LENGTH_SHORT).show()
            }

    }
}