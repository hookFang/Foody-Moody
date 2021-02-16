package com.bluegeeks.foodymoody.entity
class User {

    //Properties
    var id: String? = null
    var email: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var userName: String? = null
    var birthDay: String? = null
    var photoURI: String? = null
    var followers: ArrayList<String>? = ArrayList()
    var following:  ArrayList<String>? = ArrayList()
    var bio: String? = null
    var time: String? = null

    //Constructors
    constructor()

    constructor(
        id: String?,
        email: String?,
        firstName: String?,
        lastName: String?,
        userName: String?,
        birthDay: String?,
        photoURI: String?,
        followers: ArrayList<String>?,
        following: ArrayList<String>?,
        bio: String?,
        time: String?
    ) {
        this.id = id
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
        this.userName = userName
        this.birthDay = birthDay
        this.photoURI = photoURI
        this.followers = followers
        this.following = following
        this.bio = bio
        this.time = time
    }

    constructor(
        email: String?,
        firstName: String?,
        lastName: String?,
        userName: String?,
        birthDay: String?,
        photoURI: String?,
        followers: ArrayList<String>?,
        following: ArrayList<String>?,
        bio: String?,
        time: String?
    ) {
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
        this.userName = userName
        this.birthDay = birthDay
        this.photoURI = photoURI
        this.followers = followers
        this.following = following
        this.bio = bio
        this.time = time
    }


}