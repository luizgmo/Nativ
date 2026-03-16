package com.luiz.nativ.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.luiz.nativ.auth.UserAuth
import com.luiz.nativ.dao.UserDAO
import com.luiz.nativ.model.User
import com.luiz.nativ.utils.Base64Converter
import com.luiz.nativ.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val userAuth = UserAuth()
    private val userDAO = UserDAO()

    private val galeria = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) binding.profilePicture.setImageURI(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAlterarFoto.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSalvarPerfil.setOnClickListener {
            val email = userAuth.getEmailUsuarioLogado() ?: ""
            val user = User(
                email,
                binding.edtUsername.text.toString(),
                binding.edtFullName.text.toString(),
                Base64Converter.drawableToString(binding.profilePicture.drawable)
            )
            userDAO.salvarPerfil(user) { sucesso ->
                if (sucesso) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
        }
    }
}