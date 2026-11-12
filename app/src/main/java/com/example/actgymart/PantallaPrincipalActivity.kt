package com.example.actgymart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PantallaPrincipalActivity : AppCompatActivity() {

    private lateinit var llContenedorSuplementos: LinearLayout
    private lateinit var tvCategoriaResultados: TextView
    private var peticionActualId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_principal)

        val tvNombreUsuario = findViewById<TextView>(R.id.tvNombreUsuario)
        val btnPerson = findViewById<ImageView>(R.id.btnPerson)
        val btnHeart = findViewById<ImageView>(R.id.btnHeart)

        val cardPreEntreno = findViewById<LinearLayout>(R.id.cardPreEntreno)
        val cardProteina = findViewById<LinearLayout>(R.id.cardProteina)
        val cardCombos = findViewById<LinearLayout>(R.id.cardCombos)
        val cardCreatinas = findViewById<LinearLayout>(R.id.cardCreatinas)
        val cardFarmacos = findViewById<LinearLayout>(R.id.cardFarmacos)

        llContenedorSuplementos = findViewById(R.id.llContenedorSuplementos)
        tvCategoriaResultados = findViewById(R.id.tvCategoriaResultados)

        val usuarioLogueado = intent.getStringExtra("USUARIO_LOGUEADO") ?: ""

        if (usuarioLogueado.isNotEmpty()) {
            val admin = AdminSQLiteOpenHelper(this)
            val nombreReal = admin.obtenerNombreUsuario(usuarioLogueado)
            tvNombreUsuario.text = "Hola $nombreReal"
        }

        btnHeart.setOnClickListener {
            mostrarFavoritos()
        }

        btnPerson.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("Ir al perfil")
            popup.menu.add("Cerrar sesión")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Ir al perfil" -> {
                        val intent = Intent(this, PerfilActivity::class.java)
                        intent.putExtra("USUARIO_LOGUEADO", usuarioLogueado)
                        startActivity(intent)
                        true
                    }
                    "Cerrar sesión" -> {
                        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        cardPreEntreno.setOnClickListener {
            cargarSuplementos("pre workout", "Pre Entreno")
        }

        cardProteina.setOnClickListener {
            cargarSuplementos("protein", "Proteína")
        }

        cardCombos.setOnClickListener {
            cargarSuplementos("energy bar", "Combos")
        }

        cardCreatinas.setOnClickListener {
            cargarSuplementos("creatine", "Creatinas")
        }

        cardFarmacos.setOnClickListener {
            cargarSuplementos("bcaa", "Fármacos y Recuperación")
        }

        cargarSuplementos("protein", "Proteína")
    }

    private fun cargarSuplementos(terminoBusqueda: String, categoriaTitulo: String) {
        peticionActualId++
        val idDeEstaPeticion = peticionActualId

        tvCategoriaResultados.text = "Cargando $categoriaTitulo..."
        llContenedorSuplementos.removeAllViews()

        RetrofitClient.instance.buscarSuplementos(terminoBusqueda)
            .enqueue(object : Callback<SuplementosResponse> {
                override fun onResponse(
                    call: Call<SuplementosResponse>,
                    response: Response<SuplementosResponse>
                ) {
                    if (idDeEstaPeticion != peticionActualId) return

                    if (response.isSuccessful && response.body() != null) {
                        val productos = response.body()!!.products
                        val inflater = LayoutInflater.from(this@PantallaPrincipalActivity)
                        var agregados = 0

                        for (producto in productos) {
                            val nombre = producto.nombre ?: continue
                            if (nombre.isBlank()) continue

                            val marca = producto.marca ?: "Marca Vegana"
                            val imagenUrl = producto.imagenUrl ?: ""
                            val cantidad = producto.cantidad ?: "Presentación estándar"
                            val ingredientesReales = producto.ingredientes?.takeIf { it.isNotBlank() }
                                ?: "Ingredientes vegetales certificados por Open Food Facts."
                            val etiquetasReales = producto.etiquetas?.takeIf { it.isNotBlank() }
                                ?: "Vegan"

                            val descripcion = "Certificaciones: $etiquetasReales\n\n" +
                                    "Ingredientes del producto:\n$ingredientesReales\n\n" +
                                    "Sugerencia de uso:\n${obtenerGuiaVegana(categoriaTitulo)}"

                            val itemView = inflater.inflate(
                                R.layout.item_suplemento,
                                llContenedorSuplementos,
                                false
                            )

                            val ivSuplemento = itemView.findViewById<ImageView>(R.id.ivSuplementoItem)
                            val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreItem)
                            val tvMarca = itemView.findViewById<TextView>(R.id.tvMarcaItem)

                            tvNombre.text = nombre
                            tvMarca.text = marca

                            if (imagenUrl.isNotEmpty()) {
                                Glide.with(this@PantallaPrincipalActivity)
                                    .load(imagenUrl)
                                    .placeholder(R.drawable.img_1)
                                    .error(R.drawable.img_1)
                                    .into(ivSuplemento)
                            } else {
                                ivSuplemento.setImageResource(R.drawable.img_1)
                            }

                            itemView.setOnClickListener {
                                val intent = Intent(
                                    this@PantallaPrincipalActivity,
                                    SuplementoDetailActivity::class.java
                                )
                                intent.putExtra("NOMBRE", nombre)
                                intent.putExtra("MARCA", marca)
                                intent.putExtra("IMAGEN_URL", imagenUrl)
                                intent.putExtra("CANTIDAD", cantidad)
                                intent.putExtra("CATEGORIA", categoriaTitulo)
                                intent.putExtra("DESCRIPCION", descripcion)
                                startActivity(intent)
                            }

                            llContenedorSuplementos.addView(itemView)
                            agregados++
                        }

                        if (agregados > 0) {
                            tvCategoriaResultados.text = "Resultados: $categoriaTitulo"
                        } else {
                            tvCategoriaResultados.text = "No se encontraron resultados"
                        }
                    } else {
                        tvCategoriaResultados.text = "No se encontraron resultados"
                    }
                }

                override fun onFailure(call: Call<SuplementosResponse>, t: Throwable) {
                    if (idDeEstaPeticion != peticionActualId) return
                    tvCategoriaResultados.text = "Error de conexión"
                    Toast.makeText(
                        this@PantallaPrincipalActivity,
                        "Error al conectar con la API",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun mostrarFavoritos() {
        peticionActualId++
        llContenedorSuplementos.removeAllViews()

        val favoritos = FavoritosManager.obtenerTodos()
        if (favoritos.isEmpty()) {
            tvCategoriaResultados.text = "Aún no tienes favoritos guardados"
            Toast.makeText(this, "No tienes productos en favoritos", Toast.LENGTH_SHORT).show()
            return
        }

        tvCategoriaResultados.text = "Mis Favoritos (${favoritos.size})"
        val inflater = LayoutInflater.from(this)

        for (fav in favoritos) {
            val itemView = inflater.inflate(
                R.layout.item_suplemento,
                llContenedorSuplementos,
                false
            )

            val ivSuplemento = itemView.findViewById<ImageView>(R.id.ivSuplementoItem)
            val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreItem)
            val tvMarca = itemView.findViewById<TextView>(R.id.tvMarcaItem)

            tvNombre.text = fav.nombre
            tvMarca.text = fav.marca

            if (fav.imagenUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(fav.imagenUrl)
                    .placeholder(R.drawable.img_1)
                    .error(R.drawable.img_1)
                    .into(ivSuplemento)
            } else {
                ivSuplemento.setImageResource(R.drawable.img_1)
            }

            itemView.setOnClickListener {
                val intent = Intent(this, SuplementoDetailActivity::class.java)
                intent.putExtra("NOMBRE", fav.nombre)
                intent.putExtra("MARCA", fav.marca)
                intent.putExtra("IMAGEN_URL", fav.imagenUrl)
                intent.putExtra("CANTIDAD", fav.cantidad)
                intent.putExtra("CATEGORIA", fav.categoria)
                intent.putExtra("DESCRIPCION", fav.descripcion)
                startActivity(intent)
            }

            llContenedorSuplementos.addView(itemView)
        }
    }

    private fun obtenerGuiaVegana(categoria: String): String {
        return when (categoria) {
            "Creatinas" -> "Dosis recomendada: 0.1 g por cada kg de peso corporal al día (ej. 7 g para 70 kg). Tomar con agua o jugo natural antes o después del entrenamiento."
            "Proteína" -> "Dosis recomendada: 1.6 g a 2.2 g de proteína total por kg de peso corporal al día. Mezclar 1 scoop (30 g) en 250 ml de bebida vegetal post-entrenamiento."
            "Pre Entreno" -> "Dosis recomendada: 1 porción 20 a 30 minutos antes de entrenar con 250 ml de agua fría."
            "Combos" -> "Combinación ideal de proteínas vegetales y carbohidratos limpios para sostener energía durante la sesión y acelerar la recuperación muscular."
            else -> "Seguir la porción indicada en el envase junto con hidratación adecuada y supervisión nutricional."
        }
    }
}