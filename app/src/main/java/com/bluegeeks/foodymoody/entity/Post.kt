package com.bluegeeks.foodymoody.entity

class Post {
    //Properties
    var id: String? = null
    var userId: String? = null
    var userFullName: String? = null
    var time: String? = null
    var description: String? = null
    var post: String? = null
    var postIsPhoto: Boolean? =null
    var sharedWithUsers: ArrayList<String> = ArrayList()
    var review: HashMap<String, Int>? = HashMap<String, Int> ()

    //Constructors
    constructor()
    constructor(id: String?, userId: String?, userFullName: String?, time: String?, description: String?, post: String?, postIsPhoto: Boolean?, sharedWithUsers: ArrayList<String>, review: HashMap<String, Int>?) {
        this.id = id
        this.userId = userId
        this.userFullName = userFullName
        this.time = time
        this.description = description
        this.post = post
        this.postIsPhoto = postIsPhoto
        this.sharedWithUsers = sharedWithUsers
        this.review = review
    }

    constructor(userId: String?, userFullName: String?, time: String?, description: String?, post: String?, postIsPhoto: Boolean?, sharedWithUsers: ArrayList<String>, review: HashMap<String, Int>?) {
        this.userId = userId
        this.userFullName = userFullName
        this.time = time
        this.description = description
        this.post = post
        this.postIsPhoto = postIsPhoto
        this.sharedWithUsers = sharedWithUsers
        this.review = review
    }


}