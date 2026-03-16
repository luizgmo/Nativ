package com.luiz.nativ

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.luiz.nativ.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFirebase()
        lerDadosUsuario()
        setupListeners()
    }

    fun setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    fun setupListeners() {
        binding.btnSair.setOnClickListener {
            logout()
        }
    }

    fun lerDadosUsuario() {
        val email = firebaseAuth.currentUser!!.email.toString()
        val db = Firebase.firestore

        db.collection("usuarios").document(email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val imageString = document.data!!["fotoPerfil"].toString()
                        val bitmap = Base64Converter.stringToBitmap(imageString)

                        binding.imgHomeProfile.setImageBitmap(bitmap)
                        binding.txtUsername.text = document.data!!["username"].toString()
                        binding.txtNomeCompleto.text = document.data!!["nomeCompleto"].toString()
                    }
                } else {
                    Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun logout() {
        firebaseAuth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}