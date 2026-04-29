package com.luiz.nativ.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.luiz.nativ.R
import com.luiz.nativ.adapter.PostAdapter
import com.luiz.nativ.auth.UserAuth
import com.luiz.nativ.dao.UserDAO
import com.luiz.nativ.model.Post
import com.luiz.nativ.utils.Base64Converter
import com.luiz.nativ.utils.LocalizacaoHelper
import com.luiz.nativ.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val userAuth = UserAuth()
    private val userDAO = UserDAO()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: PostAdapter

    private val PAGE_SIZE = 5L
    private var ultimoTimestamp: Timestamp? = null
    private var carregando = false
    private var filtroCidade: String? = null

    private var imagemPostSelecionada: String? = null
    private var imagemNovoPostDialog: ImageView? = null
    private var cidadeNovoPost: String? = null

    private val LOCATION_PERMISSION_CODE = 1001

    private val galeriaPost = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imagemNovoPostDialog?.setImageURI(uri)
            imagemNovoPostDialog?.visibility = View.VISIBLE
            imagemNovoPostDialog?.drawable?.let {
                imagemPostSelecionada = Base64Converter.drawableToString(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PostAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val email = userAuth.getEmailUsuarioLogado()
        if (email != null) {
            userDAO.buscarPerfil(email) { user ->
                if (user != null) {
                    runOnUiThread {
                        binding.txtUsername.text = user.username
                        binding.txtNomeCompleto.text = user.nomeCompleto
                        if (user.fotoPerfil.isNotEmpty()) {
                            runCatching {
                                binding.imgHomeProfile.setImageBitmap(Base64Converter.stringToBitmap(user.fotoPerfil))
                            }
                        }
                    }
                }
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layout = recyclerView.layoutManager as LinearLayoutManager
                val totalItems = layout.itemCount
                val lastVisible = layout.findLastVisibleItemPosition()
                if (!carregando && lastVisible >= totalItems - 2 && filtroCidade == null) {
                    carregarFeed(paginar = true)
                }
            }
        })

        binding.edtBuscarCidade.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString().trim()
                if (texto.length >= 3) {
                    filtroCidade = texto
                    binding.btnLimparBusca.visibility = View.VISIBLE
                    buscarPorCidadeRealtime(texto)
                } else if (texto.isEmpty() && filtroCidade != null) {
                    limparBusca()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnLimparBusca.setOnClickListener {
            limparBusca()
        }

        binding.btnAdicionarPost.setOnClickListener { abrirDialogNovoPost() }
        binding.btnPerfil.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.btnSair.setOnClickListener {
            userAuth.logout()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        carregarFeed(paginar = false)
    }

    private fun limparBusca() {
        filtroCidade = null
        if (binding.edtBuscarCidade.text.isNotEmpty()) {
            binding.edtBuscarCidade.setText("")
        }
        binding.btnLimparBusca.visibility = View.GONE

        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        ultimoTimestamp = null
        adapter.setPosts(emptyList())
        carregarFeed(paginar = false)
    }

    private fun buscarPorCidadeRealtime(texto: String) {
        carregando = true
        db.collection("posts")
            .whereGreaterThanOrEqualTo("cidade", texto)
            .whereLessThanOrEqualTo("cidade", texto + "\uf8ff")
            .get()
            .addOnSuccessListener { task ->
                carregando = false
                val docs = task.documents
                if (docs.isEmpty()) {
                    adapter.setPosts(emptyList())
                } else {
                    montarPosts(docs) { posts -> adapter.setPosts(posts) }
                }
            }
            .addOnFailureListener { carregando = false }
    }

    private fun carregarFeed(paginar: Boolean) {
        if (carregando) return
        carregando = true
        if (!paginar) ultimoTimestamp = null

        var query = db.collection("posts")
            .orderBy("data", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE)

        ultimoTimestamp?.let { query = query.startAfter(it) }

        query.get().addOnSuccessListener { task ->
            carregando = false
            val docs = task.documents
            if (docs.isEmpty()) return@addOnSuccessListener
            ultimoTimestamp = docs.last().getTimestamp("data")
            montarPosts(docs) { posts ->
                if (paginar) adapter.addPosts(posts)
                else adapter.setPosts(posts)
            }
        }.addOnFailureListener { carregando = false }
    }

    private fun montarPosts(docs: List<com.google.firebase.firestore.DocumentSnapshot>, onReady: (List<Post>) -> Unit) {
        val total = docs.size
        if (total == 0) { onReady(emptyList()); return }
        val postsTemp = arrayOfNulls<Post>(total)
        var prontos = 0
        for ((index, doc) in docs.withIndex()) {
            val imageString = doc.getString("imageString").orEmpty()
            val descricao = doc.getString("descricao").orEmpty()
            val autorEmail = doc.getString("autorEmail").orEmpty()
            val cidade = doc.getString("cidade").orEmpty()
            val bitmap = if (imageString.isNotEmpty()) runCatching { Base64Converter.stringToBitmap(imageString) }.getOrNull() else null
            fun salvar(username: String, autorFoto: android.graphics.Bitmap?) {
                postsTemp[index] = Post(descricao, bitmap, autorEmail, username, autorFoto, cidade)
                prontos++
                if (prontos == total) runOnUiThread { onReady(postsTemp.filterNotNull()) }
            }
            if (autorEmail.isNotEmpty()) {
                userDAO.buscarPerfil(autorEmail) { user ->
                    val foto = user?.fotoPerfil?.let { runCatching { Base64Converter.stringToBitmap(it) }.getOrNull() }
                    salvar(user?.username ?: autorEmail, foto)
                }
            } else salvar("", null)
        }
    }

    private fun abrirDialogNovoPost() {
        imagemPostSelecionada = null
        cidadeNovoPost = null
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_novo_post, null)
        val imagemPost = dialogView.findViewById<ImageView>(R.id.imgNovoPost)
        val btnAnexar = dialogView.findViewById<Button>(R.id.btnSelecionarFotoPost)
        val descricaoInput = dialogView.findViewById<EditText>(R.id.edtDescricaoPost)
        val txtCidade = dialogView.findViewById<TextView>(R.id.txtCidadePost)
        imagemNovoPostDialog = imagemPost
        btnAnexar.setOnClickListener { galeriaPost.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        obterCidade { cidade ->
            runOnUiThread {
                if (cidade != null) {
                    cidadeNovoPost = cidade
                    txtCidade.text = "📍 $cidade"
                    txtCidade.visibility = View.VISIBLE
                }
            }
        }
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.action_adicionar, null)
            .setNegativeButton(R.string.action_cancelar, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FF7A00"))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#A0A0A0"))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val descricao = descricaoInput.text.toString().trim()
                if (descricao.isEmpty() && imagemPostSelecionada == null) return@setOnClickListener
                val emailAutor = userAuth.getEmailUsuarioLogado() ?: ""
                val postData = hashMapOf(
                    "imageString" to (imagemPostSelecionada ?: ""),
                    "descricao" to descricao,
                    "autorEmail" to emailAutor,
                    "cidade" to (cidadeNovoPost ?: ""),
                    "data" to Timestamp.now()
                )
                db.collection("posts").add(postData).addOnSuccessListener {
                    dialog.dismiss()
                    carregarFeed(paginar = false)
                }
            }
        }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun obterCidade(callback: (String?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
            callback(null)
            return
        }
        val helper = LocalizacaoHelper(this)
        helper.obterLocalizacaoAtual(object : LocalizacaoHelper.Callback {
            override fun onLocalizacaoRecebida(endereco: Address, latitude: Double, longitude: Double) {
                val cidade = endereco.locality ?: endereco.subAdminArea ?: "Desconhecida"
                callback(cidade)
            }
            override fun onErro(mensagem: String) = callback(null)
        })
    }
}