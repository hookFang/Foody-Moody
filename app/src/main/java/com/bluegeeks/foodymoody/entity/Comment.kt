package com.bluegeeks.foodymoody.entity

class Comment {
    //Properties
    var id: String? = null
    var userId: String? = null
    var userFullName: String? = null
    var postId: String? = null
    var time: String? = null
    var comment: String? = null
    var whoLiked: ArrayList<String>? = ArrayList()

    //Constructors
    constructor()
    constructor(
        id: String?,
        userId: String?,
        userFullName: String?,
        postId: String?,
        time: String?,
        comment: String?,
        whoLiked: ArrayList<String>?
    ) {
        this.id = id
        this.userId = userId
        this.userFullName = userFullName
        this.postId = postId
        this.time = time
        this.comment = comment
        this.whoLiked = whoLiked
    }

    constructor(userId: String?, userFullName: String? = null, postId: String?, time: String?, comment: String?, whoLiked: ArrayList<String>?
    ) {
        this.userId = userId
        this.userFullName = userFullName
        this.postId = postId
        this.time = time
        this.comment = comment
        this.whoLiked = whoLiked
    }
}