package com.example.ulib.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ulib.R
import com.example.ulib.databinding.ActivityPdfBinding
import com.example.ulib.models.ModelCategory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPdfBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mProgressDialog: Dialog
//    arrayList to hold pdf Categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

//    uri of picked pdf
    private  var pdfUri: Uri?=null
//    TAG
    private val TAG="PDF_ADD_TAG"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        init firebase Auth
        firebaseAuth=FirebaseAuth.getInstance()
        loadPdfCategories()

//      handle click
//        show category Pick Dialog
        binding.bookCatTv.setOnClickListener {
            categoryPickDialog()
        }
//        pick pdf intent
        binding.attachFile.setOnClickListener {
            pdfPickIntent()
        }
//        start uploading pdf/book
        binding.uploadButton.setOnClickListener {

//            step1--> validate data
//            step2--> upload pdf to firebase storage
//            step3-->get Url of uploaded pdf
//            step$-->Upload pdf into firebase db

            validateData()
        }
//        handle go back btn
        binding.backImage.setOnClickListener {
            val intent=Intent(this@PdfActivity, DashboardAdminActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private var title=""
    private var description=""
    private var category=""

    private fun validateData() {
        Log.d(TAG,"validate Data:validating Data ")

//        get data
        title=binding.bookTitleEdt.text.toString().trim()
        description=binding.bookDesEdt.text.toString().trim()
        category=binding.bookCatTv.text.toString().trim()

//        validate data
        if(title.isEmpty())
        {
            Toast.makeText(this,"Enter Title..",Toast.LENGTH_SHORT).show()
        }
        else if(description.isEmpty())
        {
            Toast.makeText(this,"Enter Description..",Toast.LENGTH_SHORT).show()
        }
        else if(category.isEmpty())
        {
            Toast.makeText(this,"Enter Category..",Toast.LENGTH_SHORT).show()
        }
        else if(pdfUri==null)
        {
            Toast.makeText(this,"Pick Pdf...",Toast.LENGTH_SHORT).show()
        }
        else
        {
//            data validated--> begin upload
            uploadPdfToStorage()

        }




    }

    private fun uploadPdfToStorage() {
        Log.d(TAG,"uploadPdfToStorage:Uploading to storage")

//        show progress dialog
        showProgressDialog("Uploading PDF..")
//        timestamp
        val timestamp=System.currentTimeMillis()

//        path of pdf in firebase storage
        val filePathAndName="Books/$timestamp"

//        storage ref
        val storageReference=FirebaseStorage.getInstance().getReference(filePathAndName)

        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot->
                Log.d(TAG,"uploadPdfToStorage: Pdf Uploaded now getting Url...")
//                set url of uploaded pdf-->step3
                val uriTask:Task<Uri> = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);
                val uploadedPdfUrl="${uriTask.result}"
                hideProgressDialog()
                uploadPdfInfoToDb(uploadedPdfUrl,timestamp)

            }
            .addOnFailureListener{
                Log.d(TAG,"uploadPdfToStorage: failed to upload due to ${it.message}")
                hideProgressDialog()
                Toast.makeText(this,"Failed upload due to ${it.message} ",Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
//    Upload pdf info to firebase db
        Log.d(TAG,"UploadPdfInfoToDb:uploading to db")
        showProgressDialog("Uploading Pdf info..")

//        uid of current user
        val uid= firebaseAuth.uid

//        setup data to upload
        val hashMap:HashMap<String,Any> = HashMap()
        hashMap["uid"]="$uid"
        hashMap["id"]="$timestamp"
        hashMap["title"]="$title"
        hashMap["description"]="$description"
        hashMap["categoryId"]="$selectedCategoryId"
        hashMap["url"]="$uploadedPdfUrl"
        hashMap["timestamp"]=timestamp
        hashMap["viewsCount"]=0
        hashMap["downloadsCount"]=0

//        db reference DB> Books >BookId >(BookInfo)
        val ref=FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"uploadedPdfInfoToDb:uploaded to db")
                hideProgressDialog()
                Toast.makeText(this,"Uploaded..",Toast.LENGTH_SHORT).show()
                pdfUri=null

            }
            .addOnFailureListener{
                Log.d(TAG,"uploadPdfInfoToDb : failed to upload due to ${it.message}")
                hideProgressDialog()
                Toast.makeText(this,"Failed upload due to ${it.message} ",Toast.LENGTH_SHORT).show()

            }


    }

    private fun loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: Loading Pdf Categories")
//        init arrayList
        categoryArrayList= ArrayList()
//        db reference to load categories DF >Categories

        val ref=FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
//                clear list before adding data
                categoryArrayList.clear()
                for(it in snapshot.children)
                {
//                    get data
                    val model=it.getValue(ModelCategory::class.java)
//                    add to array List
                    categoryArrayList.add(model!!)
                    Log.d(TAG,"on Data Change:${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private var selectedCategoryId=""
    private var selectedCategoryTitle=""

    private fun categoryPickDialog()
    {
        Log.d(TAG,"CategoryPickDialog: Showing pdf category pick dialog")

//        get string array of Categories from arrayList
        val categoriesArray= arrayOfNulls<String>(categoryArrayList.size)
        for(i in categoryArrayList.indices)
        {
            categoriesArray[i]=categoryArrayList[i].category
        }

//        alert dialog
        val builder=AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray){dialog,which->
//                handle item click
//                get clicked item
                selectedCategoryTitle=categoryArrayList[which].category
                selectedCategoryId=categoryArrayList[which].id

//                set category to textview
                binding.bookCatTv.text=selectedCategoryTitle

                Log.d(TAG,"categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG,"categoryPickDialog: Selected Category Title: $selectedCategoryTitle")
            }
            .show()
    }

    private fun pdfPickIntent(){
        Log.d(TAG,"pdfPickIntent: starting pdf pick intent")
        val intent= Intent()
        intent.type="application/pdf"
        intent.action=Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    private val pdfActivityResultLauncher=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->
            if(result.resultCode== RESULT_OK){
                Log.d(TAG,"PDF Picked")
                pdfUri=result.data!!.data
            }
            else
            {
                Log.d(TAG,"PDF pick Cancelled")
                Toast.makeText(this,"Cancelled",Toast.LENGTH_SHORT).show()
            }
        }
    )

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