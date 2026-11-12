package com.example.actgymart

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class SuplementoDetailActivity : AppCompatActivity() {

    private var esFavorito = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suplemento_detail)

        val btnVolverDetalle = findViewById<ImageView>(R.id.btnVolverDetalle)
        val btnFavorito = findViewById<ImageButton>(R.id.btnFavorito)
        val ivDetalleImagen = findViewById<ImageView>(R.id.ivDetalleImagen)
        val tvDetalleCategoria = findViewById<TextView>(R.id.tvDetalleCategoria)
        val tvDetalleNombre = findViewById<TextView>(R.id.tvDetalleNombre)
        val tvDetalleMarca = findViewById<TextView>(R.id.tvDetalleMarca)
        val tvDetalleCantidad = findViewById<TextView>(R.id.tvDetalleCantidad)
        val tvDetalleDescripcion = findViewById<TextView>(R.id.tvDetalleDescripcion)

        val nombre = intent.getStringExtra("NOMBRE") ?: "Suplemento Vegano"
        val marca = intent.getStringExtra("MARCA") ?: "Sin marca"
        val imagenUrl = intent.getStringExtra("IMAGEN_URL") ?: ""
        val cantidad = intent.getStringExtra("CANTIDAD") ?: "No especificada"
        val categoria = intent.getStringExtra("CATEGORIA") ?: "SUPLEMENTO VEGANO"
        val descripcion = intent.getStringExtra("DESCRIPCION") ?: ""

        tvDetalleCategoria.text = "100% VEGANO • ${categoria.uppercase()}"
        tvDetalleNombre.text = nombre
        tvDetalleMarca.text = "Marca: $marca"
        tvDetalleCantidad.text = "Presentación: $cantidad"
        tvDetalleDescripcion.text = descripcion

        if (imagenUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imagenUrl)
                .placeholder(R.drawable.img_1)
                .error(R.drawable.img_1)
                .into(ivDetalleImagen)
        } else {
            ivDetalleImagen.setImageResource(R.drawable.img_1)
        }

        esFavorito = FavoritosManager.esFavorito(nombre)
        actualizarIconoFavorito(btnFavorito)

        btnVolverDetalle.setOnClickListener {
            finish()
        }

        btnFavorito.setOnClickListener {
            esFavorito = !esFavorito
            if (esFavorito) {
                FavoritosManager.agregar(
                    SuplementoFavorito(nombre, marca, imagenUrl, cantidad, categoria, descripcion)
                )
                Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
            } else {
                FavoritosManager.eliminar(nombre)
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
            }
            actualizarIconoFavorito(btnFavorito)
        }
    }

    private fun actualizarIconoFavorito(btnFavorito: ImageButton) {
        if (esFavorito) {
            btnFavorito.setColorFilter(Color.parseColor("#E91E63"))
        } else {
            btnFavorito.setColorFilter(Color.WHITE)
        }
    }
}