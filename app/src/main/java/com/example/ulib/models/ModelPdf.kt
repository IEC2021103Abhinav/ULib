package com.example.ulib.models

class ModelPdf {
//    variables
    var uid:String=""
    var id:String=""
    var title:String=""
    var description:String=""
    var categoryId: String=""
    var url:String=""
    var timestamp:Long=0
    var viewsCount:Long=0
    var downloadCount:Long=0
    var isFavourite=false

//    empty constructor (required by firebase)
    constructor()
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        url: String,
        timestamp: Long,
        viewsCount: Long,
        downloadCount: Long,
        isFavourite:Boolean
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.downloadCount = downloadCount
        this.isFavourite=isFavourite
    }
//    parametrized constructor



}