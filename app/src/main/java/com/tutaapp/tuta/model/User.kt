package com.tutaapp.tuta.model

class User(
    val Id: String,
    val FirstName: String,
    val LastName: String,
    val Email: String,
    val DeletedAt: String,
    val CreatedAt: String,
    val UpdatedAt: String,
    val EmailVerifiedAt: String,
    val is_driver: Int,
    val token: String
)