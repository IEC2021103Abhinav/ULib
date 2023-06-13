package com.example.ulib.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.ulib.R
import com.example.ulib.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryAddBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth=FirebaseAuth.getInstance()
        binding.backImage.setOnClickListener {
            val intent= Intent(this@CategoryAddActivity, DashboardAdminActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.submitButton.setOnClickListener {
            validateData()
        }





    }
    private  var category=""
    private fun validateData()
    {
        category=binding.categoryEt.text.toString().trim()
        if(category.isEmpty())
        {
            Toast.makeText(this,"Enter Category..",Toast.LENGTH_SHORT).show()
        }
        else
        {
            addCategoryFirebase()
        }

    }

    private fun addCategoryFirebase() {
//        show dialog
        showProgressDialog("please wait..")
//        get timestamp
        val timestamp=System.currentTimeMillis()
//        setup data to add in firebase db
        val hashMap=HashMap<String,Any>()
        hashMap["id"]="$timestamp"
        hashMap["category"]=category
        hashMap["timestamp"]=timestamp
        hashMap["uid"]="${firebaseAuth.uid}"

//        add to firebase db : database Root>Categories>CategoryId> category info
        val ref=FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                hideProgressDialog()
                Toast.makeText(this,"Added Successfully..",Toast.LENGTH_SHORT).show()
//              added successfully
            }
            .addOnFailureListener {e->
                hideProgressDialog()
//                failure to add
                Toast.makeText(this,"Failed to add due to ${e.message}",Toast.LENGTH_SHORT).show()

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
}