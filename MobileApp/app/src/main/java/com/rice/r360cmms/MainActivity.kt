package com.rice.r360cmms

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import okhttp3.*
import java.io.IOException;
import okhttp3.Response

import okhttp3.FormBody

import okhttp3.RequestBody





class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //testing
    }

    /**
     * This function gets executed when the button is pressed.
     * Inputs: View (not used)
     * Outputs: NA
     *
     * This function executes an API call using okHtpps3 in the run function call.
     * This function also executes a GET request using okHttps3 in the sendMessage function call.
     */
    fun sendMessage(view: View) {

        val formBody: RequestBody = FormBody.Builder()
            .add("message", "Hello from App")
            .build()

        val request: Request = Request.Builder()
            .url("http://10.0.2.2:3000/api/postTest")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    println(response.headers())
                    println("Body " + (response.body()?.string() ?: "Error: No Body"));
                }
            }
        })

//        run("https://api.github.com/users/Evin1-/repos")
//
//        val request = Request.Builder()
//            .url("http://10.0.2.2:3000/api/helloworld")
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                e.printStackTrace()
//            }
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//                    println(response.headers())
//                    println(response.body().toString())
//                }
//            }
//        })
    }

    /**
     * This function executes a API call using OkHttps3
     * Input: String - represents the URL to access
     * Output: NA
     */
    private fun run(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
        })
    }

}