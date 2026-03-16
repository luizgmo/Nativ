package com.luiz.nativ

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.luiz.nativ.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFirebase()
        setupListeners()
    }

    private fun setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private val galeria = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()) {
            uri ->
        if (uri != null) {
            binding.profilePicture.setImageURI(uri)
        } else {
            Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        binding.btnAlterarFoto.setOnClickListener {
            galeria.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnSalvarPerfil.setOnClickListener {
            salvarDadosPerfil()
        }
    }

    private fun salvarDadosPerfil() {
        if (firebaseAuth.currentUser != null) {
            val email = firebaseAuth.currentUser!!.email.toString()
            val username = binding.edtUsername.text.toString()
            val nomeCompleto = binding.edtFullName.text.toString()

            val fotoPerfilString = Base64Converter.drawableToString(binding.profilePicture.drawable)

            val db = Firebase.firestore
            val dados = hashMapOf(
                "nomeCompleto" to nomeCompleto,
                "username" to username,
                "fotoPerfil" to fotoPerfilString
            )

            db.collection("usuarios").document(email)
                .set(dados)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}