package com.example.actgymart

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SuplementosApi {
    @GET("cgi/search.pl")
    fun buscarSuplementos(
        @Query("search_terms") termino: String,
        @Query("tagtype_0") tagType: String = "labels",
        @Query("tag_contains_0") tagContains: String = "contains",
        @Query("tag_0") tagValue: String = "en:vegan",
        @Query("search_simple") simple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") limite: Int = 8
    ): Call<SuplementosResponse>
}