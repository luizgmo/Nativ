package com.luiz.nativ.model

// modelo do usuario salvo no banco
data class User(
    val email: String = "",
    val username: String = "",
    val nomeCompleto: String = "",
    val fotoPerfil: String = ""
)
