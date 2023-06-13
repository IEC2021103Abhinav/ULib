package com.example.ulib.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.example.ulib.MyApplication
import com.example.ulib.R
import com.example.ulib.databinding.RowCommentBinding
import com.example.ulib.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment:RecyclerView.Adapter<AdapterComment.HolderComment> {

    //    context
//    arraylist hold comments
//    view binding
//    view-holder class

    val context:Context

    val commentArrayList:ArrayList<ModelComment>

    private  lateinit var binding: RowCommentBinding

    private lateinit var firebaseAuth: FirebaseAuth

    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.commentArrayList = commentArrayList
//        init firebase auth
        firebaseAuth= FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding= RowCommentBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
//        get data ,set data ,handle click

        val model=commentArrayList[position]
        val id=model.id
        val bookId=model.bookId
        val comment=model.comment
        val uid=model.uid
        val timestamp=model.timestamp

//        format timestamp
        val date=MyApplication.formatTimeStamp(timestamp.toLong())

//        set data
        holder.dateTv.text=date
        holder.commentTv.text=comment

//        we don't have username ,profile pic but we have user id ,so will load using that uid

        loadUserDetails(model,holder)

//        handle click,show dialog to delete comment
        holder.itemView.setOnClickListener {
//            requirements  to delete a comment
//            user must be logged in
//            uid in comment (to be deleted ) must be same as uid of current user
//            user can  delete only his own comment

            if(firebaseAuth.currentUser!=null && firebaseAuth.uid==uid)
            {
                deleteCommentDialog(model,holder)
            }
        }
    }

    private fun deleteCommentDialog(model: ModelComment, holder: AdapterComment.HolderComment) {
//        alert dialog
        val builder=AlertDialog.Builder(context)
        val bookId=model.bookId
        val commentId=model.id

        builder.setTitle("Delete Comment")
            .setMessage("are you sure you want to delete this comment?")
            .setPositiveButton("DELETE"){d,e->
//                delete comment
                val ref=FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context,"Comment Deleted Successfully",Toast.LENGTH_SHORT).show()


                    }
                    .addOnFailureListener {
                        Toast.makeText(context,"Failed to delete comment due to ${it.message}",Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("CANCEL"){d,e->
                d.dismiss()
            }.show()
    }

    private fun loadUserDetails(model: ModelComment, holder: AdapterComment.HolderComment) {
        val uid=model.uid
        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

//                    get name,profile image
                    val name="${snapshot.child("name").value}"
                    val profileImage="${snapshot.child("profileImage").value}"
//                    set data
                    holder.nameTv.text=name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.baseline_person_grey)
                            .into(holder.profileTv)
                    }
                    catch (e:Exception){
                        holder.profileTv.setImageResource(R.drawable.baseline_person_grey)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    inner class HolderComment(itemView: View):RecyclerView.ViewHolder(itemView){
//        init ui views
        val profileTv=binding.profileTv
        val nameTv=binding.nameTv
        val dateTv=binding.dateTv
        val commentTv=binding.commentTv
    }
}