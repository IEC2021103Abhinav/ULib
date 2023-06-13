package com.example.ulib.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Patterns
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.ulib.R
import com.example.ulib.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mProgressDialog: Dialog
    private var etEmail: EditText?=null
    private var etPassword: EditText?=null
    private var bLogin: Button?=null
    private var isAllDetailsChecked=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        etEmail=binding.editTextTextEmailAddress4
        etPassword=binding.editTextTextPassword2
        bLogin=binding.loginButton

        firebaseAuth=FirebaseAuth.getInstance()

        binding.signUpLink.setOnClickListener{
            val intent= Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
        binding.forgotPassword.setOnClickListener{
            val intent=Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        bLogin!!.setOnClickListener {

//            input data
//            validate data
//            Login firebase Auth
//            Check User TYpe
//            user-->move to dashboard user
//            admin-->move to dashboard admin
            if(validateLoginDetails())
            {
                val email = etEmail!!.text.toString().trim()
                val password = etPassword!!.text.toString().trim()
                showProgressDialog("Logging In")
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        checkUser()
                    }
                    .addOnFailureListener{
                        hideProgressDialog()
                        Toast.makeText(this,"Login FAiled due to ${it.message}",Toast.LENGTH_SHORT).show()
                    }
            }
        }
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

    private fun checkUser()
    {
        showProgressDialog("Checking User")
        val firebaseUser=firebaseAuth.currentUser!!
        val ref=FirebaseDatabase.getInstance().getReference("Users")
        firebaseUser.uid.let {
            ref.child(it)
                .addListenerForSingleValueEvent(object:ValueEventListener{

                    override fun onDataChange(snapshot: DataSnapshot) {
                        hideProgressDialog()
//                        get user type ex admin or user
                        val userType=snapshot.child("userType").value
                        if(userType=="user")
                        {
                            val intent=Intent(this@LoginActivity, DashBoardUserActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else if(userType=="admin"){
                            val intent=Intent(this@LoginActivity, DashboardAdminActivity::class.java)
                            startActivity(intent)
                            finish()

                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }


                })

        }
    }
    private fun validateLoginDetails(): Boolean {
        isAllDetailsChecked=checkAllDetails()

        return if(isAllDetailsChecked) {
            true
        } else {
            Toast.makeText(this,"Login Failed",Toast.LENGTH_LONG).show()
            false
        }
    }
    private fun isEmail(text: Editable?): Boolean {
        val email: CharSequence = text.toString()
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun checkAllDetails(): Boolean {
        if(etEmail?.length()==0)
        {
            etEmail?.error="Email cannot be blank"
            return false
        }
        if(!isEmail(etEmail?.editableText))
        {
            etEmail?.error="Email address is not valid"
            return false
        }
        if (etPassword!!.length() == 0) {
            etPassword!!.error = "Password cannot be blank"
            return false
        } else if (etPassword!!.length() < 8) {
            etPassword!!.error = "Password must be minimum 8 characters"
            return false
        }
        return true
    }

}