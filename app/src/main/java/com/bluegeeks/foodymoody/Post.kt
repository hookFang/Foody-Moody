package com.bluegeeks.foodymoody

class Post {
    //Properties
    var id: String? = null
    var userId: String? = null
    var userFullName: String? = null
    var time: String? = null
    var description: String? = null
    var post: String? = null
    var postIsPhoto: Boolean? =null

    //Constructors
    constructor()
    constructor(
        id: String?,
        userId: String?,
        userFullName: String?,
        time: String?,
        description: String?,
        post: String?,
        postIsPhoto: Boolean?
        ) {
        this.id = id
        this.userId = userId
        this.userFullName = userFullName
        this.time = time
        this.description = description
        this.post = post
        this.postIsPhoto = postIsPhoto
    }

    constructor(userId: String?, userFullName: String?, time: String?, description: String?, post: String?, postIsPhoto: Boolean?) {
        this.userId = userId
        this.userFullName = userFullName
        this.time = time
        this.description = description
        this.post = post
        this.postIsPhoto = postIsPhoto
    }
}