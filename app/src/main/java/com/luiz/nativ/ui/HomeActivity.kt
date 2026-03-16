package com.luiz.nativ.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luiz.nativ.auth.UserAuth
import com.luiz.nativ.dao.UserDAO
import com.luiz.nativ.utils.Base64Converter
import com.luiz.nativ.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val userAuth = UserAuth()
    private val userDAO = UserDAO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = userAuth.getEmailUsuarioLogado()
        if (email != null) {
            userDAO.buscarPerfil(email) { user ->
                if (user != null) {
                    binding.txtUsername.text = user.username
                    binding.txtNomeCompleto.text = user.nomeCompleto
                    binding.imgHomeProfile.setImageBitmap(Base64Converter.stringToBitmap(user.fotoPerfil))
                }
            }
        }

        binding.btnSair.setOnClickListener {
            userAuth.logout()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}