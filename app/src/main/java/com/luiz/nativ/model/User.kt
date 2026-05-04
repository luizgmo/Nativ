package com.luiz.nativ.model

// modelo do usuario salvo no banco
data class User(
    // email do usuario
    val email: String = "",
    // nome de usuario
    val username: String = "",
    // nome completo para exibicao
    val nomeCompleto: String = "",
    // foto de perfil em base64
    val fotoPerfil: String = ""
)
