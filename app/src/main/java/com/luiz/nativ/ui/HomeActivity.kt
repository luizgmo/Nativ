package com.luiz.nativ.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.luiz.nativ.R
import com.luiz.nativ.adapter.PostAdapter
import com.luiz.nativ.auth.UserAuth
import com.luiz.nativ.dao.UserDAO
import com.luiz.nativ.model.Post
import com.luiz.nativ.utils.Base64Converter
import com.luiz.nativ.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val userAuth = UserAuth()
    private val userDAO = UserDAO()
    private val db = FirebaseFirestore.getInstance()
    private var posts = ArrayList<Post>()
    private lateinit var adapter: PostAdapter
    private var imagemPostSelecionada: String? = null
    private var imagemNovoPostDialog: ImageView? = null

    private val galeriaPost = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imagemNovoPostDialog?.setImageURI(uri)
            imagemNovoPostDialog?.visibility = android.view.View.VISIBLE
            imagemNovoPostDialog?.drawable?.let {
                imagemPostSelecionada = Base64Converter.drawableToString(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PostAdapter(posts.toTypedArray())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

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

        // Carrega o feed automaticamente ao entrar na tela
        carregarFeed()

        binding.btnAdicionarPost.setOnClickListener {
            abrirDialogNovoPost()
        }

        binding.btnCarregarFeed.setOnClickListener {
            carregarFeed()
        }

        binding.btnSair.setOnClickListener {
            userAuth.logout()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun abrirDialogNovoPost() {
        imagemPostSelecionada = null
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_novo_post, null)
        val imagemPost = dialogView.findViewById<ImageView>(R.id.imgNovoPost)
        val botaoAnexarFoto = dialogView.findViewById<Button>(R.id.btnSelecionarFotoPost)
        val descricaoInput = dialogView.findViewById<EditText>(R.id.edtDescricaoPost)

        imagemNovoPostDialog = imagemPost

        botaoAnexarFoto.setOnClickListener {
            galeriaPost.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.title_novo_post)
            .setView(dialogView)
            .setPositiveButton(R.string.action_adicionar, null)
            .setNegativeButton(R.string.action_cancelar, null)
            .create()

        dialog.setOnShowListener {
            val botaoAdicionar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            botaoAdicionar.setOnClickListener {
                val descricao = descricaoInput.text.toString().trim()

                // Permite postar sem foto: só exige que haja texto OU imagem
                if (descricao.isEmpty() && imagemPostSelecionada == null) {
                    Toast.makeText(this, getString(R.string.msg_post_vazio), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val postData = hashMapOf(
                    "imageString" to (imagemPostSelecionada ?: ""),
                    "descricao" to descricao.ifEmpty { getString(R.string.label_descricao_placeholder) }
                )

                db.collection("posts")
                    .add(postData)
                    .addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.msg_post_sucesso), Toast.LENGTH_SHORT).show()
                        carregarFeed()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, getString(R.string.msg_post_erro), Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialog.setOnDismissListener {
            imagemNovoPostDialog = null
            imagemPostSelecionada = null
        }

        dialog.show()
    }

    private fun carregarFeed() {
        db.collection("posts").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    posts = ArrayList()
                    val documents = task.result

                    for (document in documents.documents) {
                        val imageString = document.data?.get("imageString")?.toString().orEmpty()
                        val descricao = document.data?.get("descricao")?.toString().orEmpty()
                        val bitmap = if (imageString.isNotEmpty()) {
                            runCatching { Base64Converter.stringToBitmap(imageString) }.getOrNull()
                        } else {
                            null
                        }
                        posts.add(Post(descricao, bitmap))
                    }

                    adapter.updatePosts(posts.toTypedArray())
                } else {
                    Toast.makeText(this, getString(R.string.msg_feed_erro), Toast.LENGTH_SHORT).show()
                }
            }
    }
}