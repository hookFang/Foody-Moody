package com.bluegeeks.foodymoody.entity
class Notifications {

    //Properties
    var id: String? = null
    var userId: String? = null
    var followerRequestId: String? = null
    var time: String? = null
    var isFollowing: Boolean = false


    //Constructors
    constructor()

    constructor(id: String?, userId: String?, followerRequestId: String?, time: String?, isFollowing: Boolean)
    {
        this.id = id
        this.userId = userId
        this.followerRequestId = followerRequestId
        this.time = time
        this.isFollowing = isFollowing
    }
}