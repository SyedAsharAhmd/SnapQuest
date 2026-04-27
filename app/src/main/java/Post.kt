package com.ashar.snapquest
data class Post(
    val userId: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val date: String = "",
    var likes: Int = 0,
    val comments: Int = 0,
    val profilePicture: String = ""
)