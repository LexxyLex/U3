package com.example.actgymart

import com.google.gson.annotations.SerializedName

data class Suplemento(
    @SerializedName("code")
    val id: String?,

    @SerializedName("product_name")
    val nombre: String?,

    @SerializedName("brands")
    val marca: String?,

    @SerializedName("image_front_url")
    val imagenUrl: String?,

    @SerializedName("quantity")
    val cantidad: String?,

    @SerializedName("ingredients_text")
    val ingredientes: String?,

    @SerializedName("labels")
    val etiquetas: String?
)