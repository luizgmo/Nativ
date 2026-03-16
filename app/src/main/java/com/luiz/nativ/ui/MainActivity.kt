package com.luiz.nativ.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.luiz.nativ.auth.UserAuth
import com.luiz.nativ.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val userAuth = UserAuth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogar.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val pass = binding.edtPassword.text.toString()
            userAuth.login(email, pass) { sucesso ->
                if (sucesso) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else Toast.makeText(this, "Erro no login", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCadastrar.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}