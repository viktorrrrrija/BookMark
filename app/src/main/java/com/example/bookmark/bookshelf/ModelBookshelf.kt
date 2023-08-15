package com.example.bookmark.bookshelf

class ModelBookshelf {

    private var id: String = ""
    private var title: String = ""
    private var timestamp: Long = 0
    private var uid: String = ""

    //empty constructor
    constructor()

    constructor(id: String, title: String, timestamp: Long, uid: String) {
        this.id = id
        this.title = title
        this.timestamp = timestamp
        this.uid = uid
    }

    fun getID(): String?{
        return id
    }
    fun getTitle(): String?{
        return title
    }
    fun getTimestamp(): Long?{
        return timestamp
    }
    fun getUid(): String?{
        return uid
    }




}