package com.luiz.nativ.model

import android.graphics.Bitmap

// modelo do post usado no feed
data class Post(
    // texto do post
    val descricao: String = "",
    // imagem do post em bitmap
    val imagem: Bitmap? = null,
    // email do autor
    val autorEmail: String = "",
    // nome de usuario do autor
    val autorUsername: String = "",
    // foto do autor em bitmap
    val autorFoto: Bitmap? = null,
    // cidade associada ao post
    val cidade: String = ""
)
