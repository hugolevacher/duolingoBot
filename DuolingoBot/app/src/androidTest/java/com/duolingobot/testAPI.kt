package com.duolingobot

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter

@RunWith(AndroidJUnit4::class)
class testAPI {
    @Test
    fun apiTesting(){
        val textToTranslate = "gfdgf"
        val tt = translateText(textToTranslate,"es","en")
        try{
            println(tt)
        }catch (e:Exception){
            println(e.message)
        }
    }

    fun translateText(text: String, sourceLang: String, targetLang: String): String? {
        val url = URL("http://10.0.2.2:5000/translate") // Your local LibreTranslate server
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        // Create the JSON payload
        val jsonInputString = """
        {
            "q": "$text",
            "source": "$sourceLang",
            "target": "$targetLang",
            "format": "text"
        }
    """.trimIndent()

        // Write the JSON payload to the output stream
        connection.outputStream.use { os ->
            val writer = OutputStreamWriter(os, "UTF-8")
            writer.write(jsonInputString)
            writer.flush()
        }

        val response = connection.inputStream.bufferedReader().use { it.readText() }

        // Parse the JSON response and extract the translatedText field
        val jsonObject = JSONObject(response)
        return jsonObject.getString("translatedText")
    }
}