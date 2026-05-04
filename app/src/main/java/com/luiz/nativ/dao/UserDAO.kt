package com.luiz.nativ.dao

import com.google.firebase.firestore.FirebaseFirestore
import com.luiz.nativ.model.User

// acesso aos dados do usuario no firestore
class UserDAO {
    // instancia do banco firestore
    private val db = FirebaseFirestore.getInstance()
    // nome da colecao usada no banco
    private val collection = "usuarios"

    // salva o perfil do usuario
    fun salvarPerfil(user: User, callback: (Boolean) -> Unit) {
        db.collection(collection).document(user.email)
            .set(user)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    // busca o perfil pelo email
    fun buscarPerfil(email: String, callback: (User?) -> Unit) {
        db.collection(collection).document(email).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                callback(user)
            }
            .addOnFailureListener { callback(null) }
    }
}
