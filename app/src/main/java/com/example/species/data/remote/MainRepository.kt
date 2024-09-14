package com.example.species.data.remote

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.species.data.modals.bird.BirdModal
import com.example.species.data.modals.leaf.Data
import com.example.species.data.modals.leaf.LeafModal
import com.example.species.presentation.components.BirdScreen
import com.example.species.presentation.components.LeafScreen
import com.example.species.presentation.modals.MainScreens
import com.google.gson.Gson

class MainRepository {

    suspend fun LeafInfo(
        context: Context,
        query: String,
        leafData: MutableState<Data?>?
    ): MutableState<Data?>? {
        var res : Data? = null
        val gson = Gson()
        val formattedQuery = query.replace("_", " ")
        println("Myquery: " + formattedQuery)
        val url = "https://script.google.com/macros/s/AKfycby8ugvraxYvSWmMsj3ZYx0znl8y72Hx8bRCCBqYXWoPZqS_MjqJ-t7U3t3pGoKwkdPC/exec?title=$formattedQuery"
        val queue = Volley.newRequestQueue(context)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val leaf = gson.fromJson(response.toString(), LeafModal::class.java)
//                res = Data("", "https://media.istockphoto.com/photos/mango-tree-leaves-picture-id1281931947", "Placeholder")
                leafData?.value = leaf.data[0]
                LeafScreen.leafInfo.value = leaf.data[0]
                println(leafData?.value.toString())
            }, {
                Toast.makeText(context, "Unable to fetch data!!!", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(jsonObjectRequest)
        return leafData
    }

    suspend fun BirdsInfo(
        context: Context,
        query: String,
        birdData: MutableState<com.example.species.data.modals.bird.Data?>?
    ): MutableState<com.example.species.data.modals.bird.Data?>? {
        BirdScreen.birdDataNotFound.value = 0
        val gson = Gson()
        val url = "https://script.google.com/macros/s/AKfycbzyEEGh5Mv8t4MkRbB6VFzzq7lm1M9NfvU6iZqk13gXK-K5Zk1ceVF6lhhwIwuOKhIL7Q/exec?scientific=$query"
        val queue = Volley.newRequestQueue(context)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                println("Query: " + query)
                val bird = gson.fromJson(response.toString(), BirdModal::class.java)
                println(bird)
//                res = Data("", "https://media.istockphoto.com/photos/mango-tree-leaves-picture-id1281931947", "Placeholder")
                if(bird.data.isNotEmpty()){
                    birdData?.value = bird.data[0]
                    BirdScreen.birdInfo.value = bird.data[0]
                    BirdScreen.birdDataNotFound.value = 1
                } else {
                    BirdScreen.birdDataNotFound.value = -1
                }
                println(birdData?.value.toString())
            }, {
                Toast.makeText(context, "Unable to fetch data!!!", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(jsonObjectRequest)
        return birdData
    }

}