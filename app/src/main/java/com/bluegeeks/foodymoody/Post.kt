package com.bluegeeks.foodymoody

class Post {
    //Properties
    var id: String? = null
    var userId: String? = null
    var time: String? = null
    var description: String? = null
    var review: HashMap<Int, String>? = HashMap<Int, String> ()
    var post: String? = null
    var postIsPhoto: Boolean? =null

    //Constructors
    constructor()
    constructor(
        id: String?,
        userId: String?,
        time: String?,
        description: String?,
        review: HashMap<Int, String>? = HashMap<Int, String> (),
        post: String?,
        postIsPhoto: Boolean?
        ) {
        this.id = id
        this.userId = userId
        this.time = time
        this.description = description
        this.review = review
        this.post = post
        this.postIsPhoto = postIsPhoto
    }

    constructor(userId: String?, time: String?, description: String?, review:  HashMap<Int, String>?, post: String?, postIsPhoto: Boolean?) {
        this.userId = userId
        this.time = time
        this.description = description
        this.review = review
        this.post = post
        this.postIsPhoto = postIsPhoto
    }
}