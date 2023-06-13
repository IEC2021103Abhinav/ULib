package com.example.ulib.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.ulib.BooksUserFragment
import com.example.ulib.databinding.ActivityDashboardUserBinding
import com.example.ulib.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashBoardUserActivity : AppCompatActivity() {
    private lateinit var binding:ActivityDashboardUserBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth=FirebaseAuth.getInstance()
        checkUser()
        setUpWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.powerImg.setOnClickListener{
            firebaseAuth.signOut()
            Toast.makeText(this,"Logout Successfully",Toast.LENGTH_SHORT).show()
            val intent=Intent(this@DashBoardUserActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.profileBtn.setOnClickListener {
            val intent=Intent(this,ProfileActivity::class.java)
            startActivity(intent)
        }

    }
    @SuppressLint("SuspiciousIndentation")
    private fun setUpWithViewPagerAdapter(viewPager: ViewPager){
        viewPagerAdapter= ViewPagerAdapter(supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this)
//        init list
        categoryArrayList= ArrayList()
//        load categories from db
        val ref=FirebaseDatabase.getInstance().getReference("Categories")
            ref.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
//                    clear list
                    categoryArrayList.clear()
//                    load some static categories ex: all,most viewed , most downloaded
//                    add data to models
                    val modelAll= ModelCategory("01","All",1,"")
                    val modelMostViewed= ModelCategory("01","Most Viewed",1,"")
                    val modelMostDownload= ModelCategory("01","Most Downloaded",1,"")

//                    add to list
                    categoryArrayList.add(modelAll)
                    categoryArrayList.add(modelMostViewed)
                    categoryArrayList.add(modelMostDownload)
//                    add to ViewPagerAdapter

                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${modelAll.id}",
                            "${modelAll.category}",
                            "${modelAll.uid}"
                        ),modelAll.category
                    )

                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${modelMostViewed.id}",
                            "${modelMostViewed.category}",
                            "${modelMostViewed.uid}"
                        ),modelMostViewed.category
                    )

                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${modelMostDownload.id}",
                            "${modelMostDownload.category}",
                            "${modelMostDownload.uid}"
                        ),modelMostDownload.category
                    )

//                    refresh list

                    viewPagerAdapter.notifyDataSetChanged()
//                    now load from firebase db
                    for( i in snapshot.children){
//                        get data in model
                        val model = i.getValue(ModelCategory::class.java)
//                        add to list
                        categoryArrayList.add(model!!)
//                        add to viewPagerAdapter
                        viewPagerAdapter.addFragment(
                            BooksUserFragment.newInstance(
                                "${model.id}",
                                "${model.category}",
                                "${model.uid}"
                            ),model.category

                        )
//                        refresh List
                        viewPagerAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
//        setup adapter to viewPager
        viewPager.adapter=viewPagerAdapter
    }
    class ViewPagerAdapter(fm:FragmentManager,behavior: Int,context: Context):FragmentPagerAdapter(fm,behavior){
//        holds list of fragments that is new instances of same fragment for each category
        private val fragmentList:ArrayList<BooksUserFragment> = ArrayList()
//        list of titles of categories ,for tabs
        private val fragmentTitleList:ArrayList<String> = ArrayList()
        private   val context:Context
        init {
            this.context=context
        }
        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragment, title:String){
//            add fragment that will be passed as parameter in fragmentList
            fragmentList.add(fragment)
//            add title that will be passed as parameter
            fragmentTitleList.add(title)
        }

    }
    @SuppressLint("SetTextI18n")

//    this activity can be opened with or without login ,so hide logout and profile btn

    private fun checkUser()
    {
        val firebaseUser=firebaseAuth.currentUser
        if(firebaseUser==null)
        {
//            not logged in  user can stay in user dashboard page  without login to
            binding.subTitleTv.text="Not Logged In"
//            hide profile and logout btn
            binding.profileBtn.visibility= View.GONE
            binding.powerImg.visibility=View.GONE


        }
        else{
//            logged in and show user info
            val email=firebaseUser.email
            binding.subTitleTv.text=email

            binding.profileBtn.visibility= View.VISIBLE
            binding.powerImg.visibility=View.VISIBLE


        }
    }
}