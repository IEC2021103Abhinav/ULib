package com.example.ulib.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.ulib.filters.FilterPdfUser
import com.example.ulib.MyApplication
import com.example.ulib.activities.PdfDetailActivity
import com.example.ulib.databinding.RowPdfUserBinding
import com.example.ulib.models.ModelPdf

class AdapterPdfUser: RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>,Filterable {

//    context , get using constructor
    private  var context:Context
//    arraylist to holdPdf,get using constructor
    public var pdfArrayList: ArrayList<ModelPdf>

//    array list to hold filtered pdfs
    public var filterList:ArrayList<ModelPdf>

    private lateinit var binding:RowPdfUserBinding

    private var filter: FilterPdfUser?=null


//    now we will create a filter class to enable searching

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList=pdfArrayList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        binding= RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPdfUser(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter==null)
        {
            filter= FilterPdfUser(filterList,this)
        }
        return filter as FilterPdfUser
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
//        get data ,set data ,handle click

//        get data
        val model=pdfArrayList[position]
        val bookId=model.id
        val categoryId=model.categoryId
        val title=model.title
        val description=model.description
        val uid=model.uid
        val url=model.url
        val timestamp=model.timestamp

//        convert time
        val date= MyApplication.formatTimeStamp(timestamp)

//        set data
        holder.titleTv.text=title
        holder.descriptionTv.text=description
        holder.dateTv.text=date

        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)
//        no need number of pages so pass null
        MyApplication.loadCategory(categoryId, holder.categoryTv)
        MyApplication.loadPdfSize(url, title, holder.sizeTv)

        holder.itemView.setOnClickListener {
//            pass bookId in intent that will be used to get pdf info
            val intent= Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId",bookId)
            context.startActivity(intent)
        }
    }

    //    ViewHolder class row _pdf_user
    inner class HolderPdfUser(itemView: View):RecyclerView.ViewHolder(itemView){
//    init ui components of row_pdf_user.xml
        var pdfView=binding.pdfView
        var progressBar=binding.ProgressBar
        var titleTv=binding.titleTv
        var descriptionTv=binding.descriptionTv
        var categoryTv=binding.categoryTv
        var sizeTv=binding.sizeTv
        var dateTv=binding.dateTv


    }


}