package com.example.actgymart

data class SuplementoFavorito(
    val nombre: String,
    val marca: String,
    val imagenUrl: String,
    val cantidad: String,
    val categoria: String,
    val descripcion: String
)

object FavoritosManager {
    private val listaFavoritos = mutableListOf<SuplementoFavorito>()

    fun agregar(item: SuplementoFavorito) {
        if (!esFavorito(item.nombre)) {
            listaFavoritos.add(item)
        }
    }

    fun eliminar(nombre: String) {
        listaFavoritos.removeAll { it.nombre == nombre }
    }

    fun esFavorito(nombre: String): Boolean {
        return listaFavoritos.any { it.nombre == nombre }
    }

    fun obtenerTodos(): List<SuplementoFavorito> {
        return listaFavoritos.toList()
    }
}