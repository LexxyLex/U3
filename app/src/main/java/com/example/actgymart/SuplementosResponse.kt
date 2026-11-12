package com.example.actgymart

import com.google.gson.annotations.SerializedName

data class SuplementosResponse(
    @SerializedName("products")
    val products: List<Suplemento>
)