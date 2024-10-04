import java.io.File

fun main() {
    val inputFileName = "liste.txt"
    val outputFileName = "formatedlistev2.txt"

    reformatWordList(inputFileName, outputFileName)
}

fun reformatWordList(inputFileName: String, outputFileName: String) {
    val lines = File(inputFileName).readLines()
    val wordMap = mutableMapOf<String, String>()

    var i = 0
    while (i < lines.size) {
        val word = lines[i].trim()
        val translations = lines[i + 1].trim()

        val cleanedTranslations = translations.replace(Regex("\\s*\\(.*?\\)\\s*"), "")

        val formattedTranslation = cleanedTranslations.split(",").joinToString("/") { it.trim() }

        wordMap[word] = formattedTranslation

        i += 3
    }

    val sortedWordMap = wordMap.toSortedMap()

    File(outputFileName).printWriter().use { out ->
        sortedWordMap.forEach { (word, translation) ->
            out.println("$word,$translation")
        }
    }

    println("Reformatted and sorted list saved to $outputFileName")
}