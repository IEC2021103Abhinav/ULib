package com.example.ulib.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ulib.MyApplication
import com.example.ulib.activities.PdfDetailActivity
import com.example.ulib.databinding.RowPdfFavBinding
import com.example.ulib.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFav  :RecyclerView.Adapter<AdapterPdfFav.HolderPdfFav>{
    private val context: Context
    private var booksArrayList:ArrayList<ModelPdf>
    private lateinit var binding: RowPdfFavBinding

    constructor(context: Context, booksArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.booksArrayList = booksArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterPdfFav.HolderPdfFav {
//   bind  && inflate layout row_xml
        binding= RowPdfFavBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPdfFav(binding.root)
    }

    override fun getItemCount(): Int {
        return booksArrayList.size
    }

    override fun onBindViewHolder(holder: AdapterPdfFav.HolderPdfFav, position: Int) {
//        get data, set data ,handle clicks

//        get data
        val model=booksArrayList[position]
        loadBookDetails(model,holder)

        //handle click , open pdf details page ,pass book id to load details
        holder.itemView.setOnClickListener {
            val intent=Intent(context,PdfDetailActivity::class.java)
            intent.putExtra("bookId",model.id)
            context.startActivity(intent)
        }
        holder.removeFavBtn.setOnClickListener {
            MyApplication.removeFromFav(context, model.id)
        }


    }

    @SuppressLint("SuspiciousIndentation")
    private fun loadBookDetails(model: ModelPdf, holder: AdapterPdfFav.HolderPdfFav) {
        val bookId=model.id
        val ref=FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val categoryId="${snapshot.child("categoryId").value}"
                        val description="${snapshot.child("description").value}"
                        val downloadsCount="${snapshot.child("downloadsCount").value}"
                        val timestamp="${snapshot.child("timestamp").value}"
                        val title="${snapshot.child("title").value}"
                        val uid="${snapshot.child("uid").value}"
                        val url="${snapshot.child("url").value}"
                        val viewsCount="${snapshot.child("viewsCount").value}"

//                        set data to model
                        model.isFavourite=true
                        model.title=title
                        model.description=description
                        model.categoryId=categoryId
                        model.timestamp=timestamp.toLong()
                        model.uid=uid
                        model.url=url
                        model.viewsCount=viewsCount.toLong()
                        model.downloadCount=downloadsCount.toLong()

                        val formattedDate=MyApplication.formatTimeStamp(timestamp.toLong())


//        load further details like category ,pdf from Url,pdf size
                        MyApplication.loadCategory(categoryId, holder.categoryTv)

//        we don't need page number here,pass null for page number \\ load pdf thumbnails
                        MyApplication.loadPdfFromUrlSinglePage(
                            url,
                            title,
                            holder.pdfView,
                            holder.progressBar,
                            null
                        )

//        load pdf size
                        MyApplication.loadPdfSize(url, title, holder.sizeTv)

                        //        set data

                        holder.titleTv.text=title
                        holder.descriptionTv.text=description
                        holder.dateTv.text=formattedDate

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

    }

    //    viewHolder Class for row_pdf_fav
    inner class HolderPdfFav(itemView: View): RecyclerView.ViewHolder(itemView){
        //        ui views of row_pdf_admin
        val pdfView=binding.pdfView
        val progressBar=binding.ProgressBar
        val titleTv=binding.titleTv
        val descriptionTv=binding.descriptionTv
        val categoryTv=binding.categoryTv
        val sizeTv=binding.sizeTv
        val dateTv=binding.dateTv
        val removeFavBtn=binding.favoriteBtn
    }


}