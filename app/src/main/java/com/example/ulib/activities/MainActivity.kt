package com.example.ulib.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.ulib.R
import com.example.ulib.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var loginBtn:Button?=null
    var nextBtn:Button?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginBtn=findViewById(R.id.login_btn)
        nextBtn=findViewById(R.id.next_btn)
        binding.loginBtn.setOnClickListener {
            val intent= Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }
        binding.nextBtn.setOnClickListener {
            val intent=Intent(this, DashBoardUserActivity::class.java)
            startActivity(intent)
        }


    }
}