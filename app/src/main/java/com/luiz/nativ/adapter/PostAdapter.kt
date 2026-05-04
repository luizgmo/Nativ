package com.luiz.nativ.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luiz.nativ.R
import com.luiz.nativ.model.Post

// adapter do feed que liga dados do post ao item da lista
class PostAdapter(private var posts: MutableList<Post> = mutableListOf()) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // guarda as views do layout de item para reutilizar
    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAutorPerfil: ImageView = view.findViewById(R.id.imgAutorPerfil)
        val txtAutorUsername: TextView = view.findViewById(R.id.txtAutorUsername)
        val txtCidade: TextView = view.findViewById(R.id.txtCidade)
        val imgPost: ImageView = view.findViewById(R.id.imgPost)
        val txtDescricao: TextView = view.findViewById(R.id.txtDescricao)
    }

    // cria o view holder inflando o layout do item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    // preenche o item com os dados do post
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // nome do autor ou email como fallback
        holder.txtAutorUsername.text = post.autorUsername.ifEmpty { post.autorEmail }
        // foto do autor ou imagem padrao
        if (post.autorFoto != null) {
            holder.imgAutorPerfil.setImageBitmap(post.autorFoto)
        } else {
            holder.imgAutorPerfil.setImageResource(R.drawable.empty_profile)
        }

        // mostra a cidade se existir
        if (post.cidade.isNotEmpty()) {
            holder.txtCidade.text = "📍 ${post.cidade}"
            holder.txtCidade.visibility = View.VISIBLE
        } else {
            holder.txtCidade.visibility = View.GONE
        }

        // mostra a imagem do post se existir
        if (post.imagem != null) {
            holder.imgPost.setImageBitmap(post.imagem)
            holder.imgPost.visibility = View.VISIBLE
        } else {
            holder.imgPost.visibility = View.GONE
        }

        // texto do post
        holder.txtDescricao.text = post.descricao
    }

    override fun getItemCount(): Int = posts.size

    // substitui toda a lista e atualiza a tela
    fun setPosts(newPosts: List<Post>) {
        posts = newPosts.toMutableList()
        notifyDataSetChanged()
    }

    // adiciona novos itens para paginacao
    fun addPosts(newPosts: List<Post>) {
        val start = posts.size
        posts.addAll(newPosts)
        notifyItemRangeInserted(start, newPosts.size)
    }
}
