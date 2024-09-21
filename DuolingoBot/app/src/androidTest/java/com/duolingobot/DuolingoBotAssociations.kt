package com.duolingobot


import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.platform.app.InstrumentationRegistry
import android.content.Context
import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DuolingoBotAssociations {
    @Test
    fun bot() {
        val dictionary= createDictionary(InstrumentationRegistry.getInstrumentation().targetContext)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        while(true){
            val indicesJDisponibles= mutableListOf(7,9,11,13,15)
            val listeDeMotFrancais = MutableList<String?>(5){null}
            for(i in 8..16 step 2){
                val elementEspagnol = device.findObject(UiSelector().className("android.widget.LinearLayout").index(i))
                val motEspagnol=elementEspagnol.getChild(UiSelector().className("android.widget.TextView")).text

                val motFrancaisATrouve: Set<String>? = dictionary[motEspagnol]?.toSet()
                for (j in indicesJDisponibles.indices){
                    var motFrancais:String?
                    if(listeDeMotFrancais[j]!=null) {
                        motFrancais = listeDeMotFrancais[j]
                    } else{
                        val elementFrancais=device.findObject(UiSelector().className("android.widget.LinearLayout").index(indicesJDisponibles[j]))
                        motFrancais=elementFrancais.getChild(UiSelector().className("android.widget.TextView")).text
                        listeDeMotFrancais[j]=motFrancais
                    }

                    if(motFrancaisATrouve!!.contains(motFrancais)){
                        elementEspagnol.click()
                        val elementFrancais=device.findObject(UiSelector().className("android.widget.LinearLayout").index(indicesJDisponibles[j]))
                        elementFrancais.click()
                        indicesJDisponibles.remove(j)
                        break
                    }
                }
            }
        }
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
                dictionary[espagnol]=francais
            }
        }
    }
    return dictionary
    }
}