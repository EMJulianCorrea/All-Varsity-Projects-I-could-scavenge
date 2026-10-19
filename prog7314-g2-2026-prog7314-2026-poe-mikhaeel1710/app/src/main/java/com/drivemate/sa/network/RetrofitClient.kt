package com.drivemate.sa.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://drivemate-api-mm-2026-byaxg8avehbpbwbv.southafricanorth-01.azurewebsites.net/"

    val api: DriveMateApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DriveMateApiService::class.java)
    }
}