package com.example.ulib

import android.annotation.SuppressLint
import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import android.view.View
import android.view.WindowId
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.ulib.activities.PdfDetailActivity
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Locale

class MyApplication:Application() {

    override fun onCreate() {
        super.onCreate()
    }
    companion object{

//        created a static method to convert timestamp to proper date format ,so we can use it
//        everywhere in project ,no need to rewrite again

        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timestamp
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return dateFormat.format(cal.time).toString()
        }

//        fun to get PDf Size
@SuppressLint("SetTextI18n")
fun loadPdfSize(pdfUrl:String, pdfTitle:String, sizeTv:TextView){
            val TAG="PDF_SIZE_TAG"
//          using url we can get file and its metadata from firebase storage
            val ref=FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener {
                    Log.d(TAG,"loadPdfSize: got metadata")
                    val bytes=it.sizeBytes.toDouble()
                    Log.d(TAG,"loadPdfSize:Size Bytes $bytes")
//                    convert bytes to KB
                    val kb=bytes/1024
                    val mb=kb/1024
                    if(mb>=1)
                    {
                        sizeTv.text="${String.format("%.2f",mb)}MB"
                    }
                    else if(kb>=1)
                    {
                        sizeTv.text="${String.format("%.2f",kb)}KB"
                    }
                    else{
                        sizeTv.text="${String.format("%.2f,bytes")}bytes"
                    }
                }
                .addOnFailureListener{e->
//                    failed to get metadata
                    Log.d(TAG,"loadPdfSize:Failed to get metadata due to ${e.message}")
                }


        }

//        Instead of making new function loadPdfPageCount() to just load pages count ,it  would be
//        more good to use some existing function to do that loadPdfFromSinglePage
//        we will add another parameter of type TextView ex-->PagesTv
//        Whenever we call that fun
//        1) if we require page numbers we will pass pageTv
//        2) if we don't require page number we will pass null
//        Add in function if PageTv(TextView) parameter is not null we will set the page-number count

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle:String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv:TextView?
        ){
            val TAG="PDF_THUMBNAIL_TAG"
//            using url we can get file and its metadata from firebase storage
            val ref=FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener {bytes->
                    Log.d(TAG,"loadPdfFromUrlSinglePage:Size Bytes $bytes")

//                    Set to PDFView
                    pdfView.fromBytes(bytes)
                        .pages(0)  //show first page only
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError{t->
                            progressBar.visibility=View.INVISIBLE
                            Log.d(TAG,"loadPdfFromUrlSinglePage:${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility=View.INVISIBLE
                            Log.d(TAG,"loadPdfFromUrlSinglePage:${t.message}")
                        }
                        .onLoad{nbPages->
                            Log.d(TAG,"loadPdfUrlFromSinglePage:Pages:$nbPages")
//                            pdf loaded ,we can set page count,pdf thumbnails
                            progressBar.visibility=View.INVISIBLE

//                            if pagesTv param is not null then set page numbers
                            if(pagesTv !=null)
                            {
                                pagesTv.text="$nbPages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener{e->
//                    failed to get metadata
                    Log.d(TAG,"loadPdfSize:Failed to get metadata due to ${e.message}")
                }
        }
        fun loadCategory(categoryId: String,categoryTv:TextView)
        {
//            load category using category id from firebase
            val ref=FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
//                        get category
                        val category="${snapshot.child("category").value}"
//                        set category
                        categoryTv.text=category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

        }

        fun deleteBook(context:Context,bookId:String,bookUrl:String,bookTitle:String)
        {
//            parameter details
//            1 context,used when require ex: for progress dialog ,toast
//            2 bookId,to delete book from db
//            3 bookUrl ,delete book from firebase storage
//            4 bookTitle ,show in dialog etc

            val TAG="DELETE_BOOK_TAG"
            Log.d(TAG,"deleteBook:deleting..")
            val progressDialog=ProgressDialog(context)
            progressDialog.setTitle("please wait")
            progressDialog.setMessage("Deleting $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG,"deleteBook:Deleting from storage..")
            val storageRef=FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageRef.delete()
                .addOnSuccessListener {
                    Log.d(TAG,"deleteBook: Deleted from storage")
                    Log.d(TAG,"deleteBook: Deleting from db now..")
                    val ref=FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context,"Successfully Deleted..",Toast.LENGTH_SHORT).show()
                            Log.d(TAG,"deleteBook:Deleted from db too..")
                        }
                        .addOnFailureListener{e->
                            progressDialog.dismiss()
                            Log.d(TAG,"deleteBook: Failed to delete from db due to ${e.message}")
                            Toast.makeText(context,"Failed to delete  due to ${e.message}",Toast.LENGTH_SHORT).show()

                        }
                }
                .addOnFailureListener{e->
                    progressDialog.dismiss()
                    Log.d(TAG,"deleteBook: Failed to delete from storage due to ${e.message}")
                    Toast.makeText(context,"Failed to delete due to ${e.message}",Toast.LENGTH_SHORT).show()
                }


        }

        fun incrementBookViewsCount(bookId:String)
        {
//            get current book views count
            val ref=FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
//                        get views count
                        var viewsCount="${snapshot.child("viewsCount").value}"
                        if(viewsCount==""|| viewsCount=="null")
                        {
                           viewsCount="0"
                        }
//                        increment views count
                        val newViewsCount=viewsCount.toLong()+1
//                        setup data to update in db
                        val hashMap=HashMap<String,Any>()
                        hashMap["viewsCount"]=newViewsCount
//                        set to db
                        val dbRef=FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

        public fun removeFromFav(context: Context,bookId: String){
            val TAG ="REMOVE_FAV_TAG"
            Log.d(TAG,"removeFromFav:Removing From Fav..")
            val firebaseAuth=FirebaseAuth.getInstance()
            val ref=FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favourites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG,"removeFromFavorite: Removed From fav")
                    Toast.makeText(context,"Removed from Favourite",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.d(TAG,"removeFromFavourite: Failed to remove from fav due to ${it.message}")
                    Toast.makeText(context,"Failed to remove from fav due to ${it.message}",Toast.LENGTH_SHORT).show()
                }

        }
    }

}