package com.luiz.nativ.auth

import com.google.firebase.auth.FirebaseAuth

// camada simples de autenticacao com firebase
class UserAuth {
    // instancia do firebase auth
    private val auth = FirebaseAuth.getInstance()

    // faz login com email e senha
    fun login(email: String, pass: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task -> callback(task.isSuccessful) }
    }

    // cria conta com email e senha
    fun cadastro(email: String, pass: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }

    // atualiza a senha do usuario logado
    fun atualizarSenha(novaSenha: String, callback: (Boolean, String?) -> Unit) {
        auth.currentUser?.updatePassword(novaSenha)
            ?.addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            } ?: callback(false, "Usuário não autenticado")
    }

    // devolve o email do usuario atual
    fun getEmailUsuarioLogado(): String? = auth.currentUser?.email

    // encerra a sessao atual
    fun logout() = auth.signOut()
}
