package com.bluegeeks.foodymoody

class User {
    //Properties
    var id: String? = null
    var email: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var userName: String? = null
    var photoURI: String? =null

    //Constructors
    constructor()
    constructor(
        id: String?,
        email: String?,
        firstName: String?,
        lastName: String?,
        userName: String?
    ) {
        this.id = id
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
        this.userName = userName
    }


    constructor(email: String?, firstName: String?, lastName: String?, userName: String?) {
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
        this.userName = userName
    }

}