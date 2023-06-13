package com.example.ulib.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.ulib.filters.FilterPdfAdmin
import com.example.ulib.MyApplication
import com.example.ulib.activities.PdfDetailActivity
import com.example.ulib.activities.PdfEditActivity
import com.example.ulib.databinding.RowPdfAdminBinding
import com.example.ulib.models.ModelPdf

class AdapterPdfAdmin :RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>,Filterable{

//    context
    private var context:Context

//    arrayList to hold pdfs
    public var pdfArrayList:ArrayList<ModelPdf>
    private val filterList:ArrayList<ModelPdf>

    private lateinit var binding:RowPdfAdminBinding

//    filter object
     private var filter: FilterPdfAdmin? =null // private will work
    //    constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList=pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
//   bind  && inflate layout row_xml
        binding=RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPdfAdmin(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
//        get data, set data ,handle clicks

//        get data
        val model=pdfArrayList[position]
        val pdfId=model.id
        val categoryId=model.categoryId
        val title=model.title
        val description=model.description
        val pdfUrl=model.url
        val timestamp=model.timestamp
        //        convert timestamp to dd/MM/yyyy format
        val formattedDate= MyApplication.formatTimeStamp(timestamp)
//        set data
        holder.titleTv.text=title
        holder.descriptionTv.text=description
        holder.dateTv.text=formattedDate

//        load further details like category ,pdf from Url,pdf size
        MyApplication.loadCategory(categoryId, holder.categoryTv)

//        we don't need page number here,pass null for page number \\ load pdf thumbnails
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

//        load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

//        handle click ,show dialog with option 1> edit book and 2>delete book

        holder.moreBtn.setOnClickListener{
            moreOptionDialog(model,holder)
        }

//        handle item  click for pdf detail
        holder.itemView.setOnClickListener {
            val intent=Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId",pdfId)
//            pdfId is used to load pdf details
            context.startActivity(intent)
        }
    }

    private fun moreOptionDialog(model: ModelPdf, holder: HolderPdfAdmin) {
//        get id,url,title of book
        val bookId=model.id
        val bookUrl=model.url
        val bookTitle=model.title

//        option to show in dialog

        val options= arrayOf("Edit","Delete")

//        alert dialog
        val builder=AlertDialog.Builder(context)
        builder.setTitle("Choose Options")
            .setItems(options){ _, position->
//                handle item click
                if(position==0)
                {
//                    edit is clicked
                    val intent= Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId",bookId)
//                    passed bookId will be used to edit the book
                    context.startActivity(intent)
                }
                else if(position==1)
                {
//                    delete is clicked ,lets create function in My Application class for this
//                    show confirmation dialog first if u need....
                    MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }.show()

    }

    override fun getFilter(): Filter {
        if(filter==null)
        {
            filter= FilterPdfAdmin(filterList,this)
        }
        return filter as FilterPdfAdmin
    }

    //    viewHolder Class for row_pdf_admin
    inner class HolderPdfAdmin(itemView:View):RecyclerView.ViewHolder(itemView){
        //        ui views of row_pdf_admin
        val pdfView=binding.pdfView
        val progressBar=binding.ProgressBar
        val titleTv=binding.titleTv
        val descriptionTv=binding.descriptionTv
        val categoryTv=binding.categoryTv
        val sizeTv=binding.sizeTv
        val dateTv=binding.dateTv
        val moreBtn=binding.moreBtn
    }


}