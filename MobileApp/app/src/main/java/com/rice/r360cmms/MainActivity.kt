package com.rice.r360cmms

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import okhttp3.*
import java.io.IOException;


class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //testing
    }
    fun sendMessage(view: View) {

        val request = Request.Builder()
            .url("https://publicobject.com/helloworld.txt")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    println(response.headers())
                    println(response.body().toString())
                }
            }
        })
    }

}