package com.example.ulib.models

import java.sql.Timestamp

class ModelCategory {
//    variables, must watch as  in firebase
//    from categoryAdd Activity
    var id:String=""
    var category:String=""
    var timestamp:Long=0
    var uid:String=""

//    empty constructor,required by firebase
    constructor()
//    parametrized constructor
    constructor(id:String,category: String,timestamp: Long,uid:String){
        this.id=id
        this.category=category
        this.timestamp=timestamp
        this.uid=uid
    }


}