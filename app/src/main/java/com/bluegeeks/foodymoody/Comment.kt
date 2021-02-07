package com.bluegeeks.foodymoody

class Comment {
    //Properties
    var id: String? = null
    var userId: String? = null
    var postId: String? = null
    var time: String? = null
    var comment: String? = null
    var review: HashMap<String, Int>? = HashMap<String, Int> ()
    var whoLiked: HashMap<String, Boolean>? = HashMap<String, Boolean> ()

    //Constructors
    constructor()
    constructor(
        id: String?,
        userId: String?,
        postId: String?,
        time: String?,
        comment: String?,
        review: HashMap<String, Int>?,
        whoLiked: HashMap<String, Boolean>?

    ) {
        this.id = id
        this.userId = userId
        this.postId = postId
        this.time = time
        this.comment = comment
        this.review = review
        this.whoLiked = whoLiked
    }

    constructor(userId: String?, postId: String?, time: String?, comment: String?, review:  HashMap<String, Int>?, whoLiked: HashMap<String, Boolean>?) {
        this.userId = userId
        this.postId = postId
        this.time = time
        this.comment = comment
        this.review = review
        this.whoLiked = whoLiked
    }
}