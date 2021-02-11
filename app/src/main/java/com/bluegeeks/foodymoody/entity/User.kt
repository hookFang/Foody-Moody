package com.bluegeeks.foodymoody.entity
class User {

    //Properties
    var id: String? = null
    var email: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var userName: String? = null
    var birthDay: String? =null
    var photoURI: String? =null

    //Constructors
    constructor()
    constructor(
        id: String?,
        email: String?,
        firstName: String?,
        lastName: String?,
        userName: String?,
        birthDay: String?,
        photoURI: String?
    ) {
        this.id = id
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
        this.userName = userName
        this.birthDay = birthDay
        this.photoURI = photoURI
    }

    constructor(email: String?, firstName: String?, lastName: String?,
                userName: String?, birthDay: String?, photoURI: String?) {
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
        this.userName = userName
        this.birthDay = birthDay
        this.photoURI = photoURI
    }

}