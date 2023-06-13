package com.example.ulib.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.ulib.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        firebaseAuth=FirebaseAuth.getInstance()
        Handler().postDelayed(Runnable {
            checkUser()
        },3000)
    }
    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
//            user not logged in go to main screen
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            firebaseUser.uid.let {
                ref.child(it)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {
//                        get user type ex admin or user
                            val userType = snapshot.child("userType").value
                            if (userType == "user") {
                                val intent =
                                    Intent(this@SplashActivity, DashBoardUserActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else if (userType == "admin") {
                                val intent =
                                    Intent(this@SplashActivity, DashboardAdminActivity::class.java)
                                startActivity(intent)
                                finish()

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }


                    })
            }
        }
    }
}