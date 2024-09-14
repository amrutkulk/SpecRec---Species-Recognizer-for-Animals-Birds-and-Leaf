package com.example.species.data.remote

import com.example.species.data.modals.TranslateModel
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class TranslateService {
    fun translate(query: String, translateTo: String): String {
        val client = OkHttpClient()

        val mediaType = MediaType.parse("application/x-www-form-urlencoded")
        val body = RequestBody.create(mediaType, "q=$query&target=$translateTo&source=en")
        val request = Request.Builder()
            .url("https://google-translate1.p.rapidapi.com/language/translate/v2")
            .post(body)
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("Accept-Encoding", "application/gzip")
            .addHeader("X-RapidAPI-Key", "a762def5b6msh625cecfa664a6b6p12ff6djsn0ec392e207b3")
            .addHeader("X-RapidAPI-Host", "google-translate1.p.rapidapi.com")
            .build()

        val response = client.newCall(request).execute()
        val gson = Gson()
        val js = JSONObject(response.body()!!.string())
        val res = gson.fromJson(
            js.toString(),
            TranslateModel::class.java
        )
        return res.data.toString()
    }
}