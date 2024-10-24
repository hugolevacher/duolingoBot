package com.duolingobot


import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.uiautomator.UiDevice
import androidx.test.platform.app.InstrumentationRegistry
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Point
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DuolingoBotAssociations {
    val dictionary= createDictionary(InstrumentationRegistry.getInstrumentation().targetContext)
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    var btnCommencer = device.findObject(By.res("com.duolingo:id/sidequestIntroStartChallenge"))
    //var btnCommencer = device.findObject(By.res("com.duolingo:id/matchMadnessStartChallenge"))
    var btnContinuer = device.findObject(By.res("com.duolingo:id/coachContinueButton"))
    private lateinit var uiObjectsFrancais: MutableMap<UiObject2, Point>
    private lateinit var uiObjectsEspagnol: MutableMap<UiObject2, Point>

    @Test
    fun bot() {
        //device.findObject(By.res("com.duolingo:id/rampUpFabIcon")).click()
        clicksStarting()
        resetUiObjects()

        while(true){
            var listeDeMotFrancais: List<String>
            var listeDeMotEspagnol: List<String>
            try{
                val (newlisteDeMotFrancais, newlisteDeMotEspagnol) = avoirListesDeMots()
                listeDeMotFrancais = newlisteDeMotFrancais
                listeDeMotEspagnol = newlisteDeMotEspagnol
            }catch(e:Exception){
                chercherSiBtnApparu()
                continue
            }


            for(i in listeDeMotEspagnol.indices){
                val centre = uiObjectsEspagnol.values.elementAt(i)
                device.swipe(centre.x, centre.y, centre.x, centre.y,1)

                val motFrancaisATrouve:Set<String>? = dictionary[listeDeMotEspagnol[i]]?.toSet()
                if(motFrancaisATrouve!=null){
                    for(index in listeDeMotFrancais.indices){
                        if(motFrancaisATrouve.contains(listeDeMotFrancais[index])){
                            val centreFrancais=uiObjectsFrancais.values.elementAt(index)
                            device.swipe(centreFrancais.x, centreFrancais.y, centreFrancais.x, centreFrancais.y,1)
                            break
                        }
                    }
                }
                else {
                    println("We need new word: ${listeDeMotEspagnol[i]}")
//                    GlobalScope.launch(Dispatchers.IO) {
//                        val motTraduit = translateText(listeDeMotEspagnol[i], "es", "fr")
//                        if (motTraduit != null) {
//                            dictionary[listeDeMotEspagnol[i]] = listOf(motTraduit)
//                        }
//                    }
                }
            }
        }
    }

    private fun avoirListesDeMots(): Pair<List<String>, List<String>> {
        val listeDeMotFrancais = uiObjectsFrancais.map { elementFrancais ->
            elementFrancais.key.text
        }
        val listeDeMotEspagnol = uiObjectsEspagnol.map { elementEspagnol ->
            elementEspagnol.key.text
        }
        return Pair(listeDeMotFrancais, listeDeMotEspagnol)
    }

    private fun chercherSiBtnApparu() {
        Thread.sleep(1000)
        btnContinuer = device.findObject(By.res("com.duolingo:id/coachContinueButton"))
        //val btnEnd = device.findObject(By.res("com.duolingo:id/sessionEndContinueButton"))
        val btnEnd = device.findObject(By.res("com.duolingo:id/primaryButton"))
        if (btnContinuer != null) {
            btnContinuer.click()
            Thread.sleep(1000)

            resetUiObjects()
        }else if(btnEnd!=null){
            btnEnd.click()
            Thread.sleep(500)
            clicksStarting()
            resetUiObjects()
        }
    }

    private fun resetUiObjects() {
        val (newUiObjectsFrancais, newUiObjectsEspagnol) = getUiObjectsFrEs(device)
        uiObjectsFrancais = newUiObjectsFrancais
        uiObjectsEspagnol = newUiObjectsEspagnol
    }

    private fun clicksStarting() {
        while (btnCommencer == null) {
            Thread.sleep(500)
            //btnCommencer = device.findObject(By.res("com.duolingo:id/matchMadnessStartChallenge"))
            btnCommencer = device.findObject(By.res("com.duolingo:id/sidequestIntroStartChallenge"))
        }
        btnCommencer.click()


        while (btnContinuer == null) {
            Thread.sleep(500)
            btnContinuer = device.findObject(By.res("com.duolingo:id/coachContinueButton"))
        }
        btnContinuer.click()
    }

    private fun getUiObjectsFrEs(device: UiDevice): Pair<MutableMap<UiObject2, Point>, MutableMap<UiObject2, Point>> {
        val allUiObject: List<UiObject2> =
            device.findObjects(By.clazz("android.widget.LinearLayout").depth(12))
        val uiObjectsFrancais = mutableMapOf<UiObject2, Point>()
        val uiObjectsEspagnol = mutableMapOf<UiObject2, Point>()
        for ((index, container) in allUiObject.withIndex()) {
            val textbox = container.findObject(By.clazz("android.widget.TextView"))
            if (index % 2 == 0) {
                uiObjectsFrancais[textbox] = textbox.visibleCenter
            } else {
                uiObjectsEspagnol[textbox] = textbox.visibleCenter
            }
        }
        return Pair(uiObjectsFrancais, uiObjectsEspagnol)
    }

    fun createDictionary(context: Context):HashMap<String,List<String>> {
        val dictionary = HashMap<String, List<String>>()
        val assetManager:AssetManager = context.assets
        val inputStream=assetManager.open("formatedliste.txt")
        val reader=BufferedReader(InputStreamReader(inputStream))

        reader.use { br->
            br.forEachLine { line ->
                val splitted = line.split(',').map { it.trim() }
                if (splitted.size == 2) {
                    val espagnol=splitted[0]
                    val francais = splitted[1].split('/').map { it.trim() }
                    if(dictionary.containsKey(espagnol)){
                        val existingList = dictionary[espagnol] ?: emptyList()
                        val combinedList = existingList + francais
                        dictionary[espagnol] = combinedList.distinct() // To avoid duplicates
                    }
                    else{
                        dictionary[espagnol]=francais
                    }
                }
            }
        }
        return dictionary
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