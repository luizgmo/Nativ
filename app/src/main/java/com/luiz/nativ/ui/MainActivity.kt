package com.luiz.nativ.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.luiz.nativ.auth.UserAuth
import com.luiz.nativ.databinding.ActivityMainBinding

// tela de login do app
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val userAuth = UserAuth()

    // inicializa a tela e verifica login automatico
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // se ja estiver logado vai direto para home
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // valida campos e tenta login
        binding.btnLogar.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val pass = binding.edtPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userAuth.login(email, pass) { sucesso ->
                if (sucesso) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // abre a tela de cadastro
        binding.btnCadastrar.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
