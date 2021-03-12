package com.bluegeeks.foodymoody.entity

import java.util.*


class ChatMessage {
    //Properties
    var chatValue: String? = null
    var sendAt: String? = null
    var senderID: String? = null

    constructor()

    constructor(chatValue: String?, sendAt: String?, senderID: String?) {
        this.chatValue = chatValue
        this.sendAt = sendAt
        this.senderID = senderID
    }
}