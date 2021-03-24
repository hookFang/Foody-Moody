package com.bluegeeks.foodymoody.entity

class ChatUsers {
    //Properties
    var id: String? = null
    var username: String? =null
    var photoURI: String? = null
    var lastMessage: String? = null
    var sendAt: String? = null

    constructor()
    constructor(id: String?, username: String?, photoURI: String?, lastMessage: String?, sendAt: String?) {
        this.id = id
        this.username = username
        this.photoURI = photoURI
        this.lastMessage = lastMessage
        this.sendAt = sendAt
    }
}