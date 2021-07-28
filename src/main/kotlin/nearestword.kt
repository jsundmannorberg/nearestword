import java.io.File
import kotlin.system.measureTimeMillis

typealias Word = String

data class WordList(
    val words: List<Word>

) {
    companion object {
        fun fromFile(name: String): WordList {
            val lines = File(name).readLines().flatMap { it.toUpperCase().split("\\s".toRegex()) }
            return WordList(lines)
        }
    }
}

interface NearestWordsAlgorithm {
    fun getWords(source: WordList, dictionary: WordList): WordList
}

interface WordDistanceAlgorithm {
    fun distance(word1: Word, word2: Word): Int
}

class SortedSmallestAverageDistance(
    private val distanceAlgorithm: WordDistanceAlgorithm
): NearestWordsAlgorithm {
    override fun getWords(source: WordList, dictionary: WordList): WordList {
        // Naive implementation
        val wordList = dictionary.words.sortedBy { word ->
            //println(averageDistance(word, source))
            averageDistance(word, source)
        }
        return WordList(wordList)
    }

    private fun averageDistance(word: Word, source: WordList): Double {
        return source.words.sumBy { distanceAlgorithm.distance(it, word) }.toDouble()/source.words.count()
    }
}

class MemoizedSortedSmallestAverageDistance(
    private val distanceAlgorithm: WordDistanceAlgorithm
): NearestWordsAlgorithm {
    override fun getWords(source: WordList, dictionary: WordList): WordList {
        val wordList = dictionary.words.sortedBy { word ->
            averageDistance(word, source)
        }
        return WordList(wordList)
    }

    private fun averageDistance(word: Word, source: WordList) : Double {
        val calculatedDistances: MutableMap<Pair<Word, Word>, Int> = HashMap()
        return source.words.sumBy { source ->
            calculatedDistances.getOrPut(Pair(word, source)) {
                distanceAlgorithm.distance(source, word)
            }
        }.toDouble()/source.words.count()
    }
}

class MemoizeFriendlySortedSmallestAverageDistance(
    private val distanceAlgorithm: WordDistanceAlgorithm
): NearestWordsAlgorithm {
    override fun getWords(source: WordList, dictionary: WordList): WordList {
        val wordList = dictionary.words.sortedBy { word ->
            averageDistance(word, source)
        }
        return WordList(wordList)
    }

    private fun averageDistance(word: Word, source: WordList): Double {
        return source.words.sumBy { distanceAlgorithm.distance(it, word) }.toDouble()/source.words.count()
    }
}

class LevenshteinDistance : WordDistanceAlgorithm {
    override fun distance(word1: Word, word2: Word): Int {
        return levenshteinDistance(word1, word2)
    }

    fun levenshteinDistance(word1: String, word2: String): Int {
        // Algorithm taken from RosettaCode
        if (word1 == word2)  return 0
        if (word1 == "") return word2.length
        if (word2 == "") return word1.length

        val v0 = IntArray(word2.length + 1) { it }  // previous
        val v1 = IntArray(word2.length + 1)         // current

        var cost: Int
        for (i in 0 until word1.length) {
            v1[0] = i + 1
            for (j in 0 until word2.length) {
                cost = if (word1[i] == word2[j]) 0 else 1
                v1[j + 1] = Math.min(v1[j] + 1, Math.min(v0[j + 1] + 1, v0[j] + cost))
            }
            for (j in 0 .. word2.length) v0[j] = v1[j]
        }
        return v1[word2.length]
    }
}

class MemoizedDistanceAlgorithm(val algorithm: WordDistanceAlgorithm) : WordDistanceAlgorithm {
    val distances: MutableMap<Pair<Word, Word>, Int> = HashMap()
    override fun distance(word1: Word, word2: Word): Int {
        return distances.getOrPut(Pair(word1, word2)) { algorithm.distance(word1, word2) }
    }
}

class DictionaryDistance(
    dictionary: WordList
): WordDistanceAlgorithm {
    val indices = dictionary.words.associate { word ->
        word to dictionary.words.indexOf(word)
    }
    override fun distance(word1: Word, word2: Word): Int {
        return Math.abs(indices[word1]!! - indices[word1]!!)
    }
}


fun main(args: Array<String>) {
    val testDictionary = WordList(File("words.txt").readLines().map { it.toUpperCase() })//WordList(listOf("AARDVARK", "DOG", "CAT", "FISH"))
    val testSource = WordList.fromFile("yesterday.txt")
    println("${testSource}")
    //val testSource = WordList(listOf("I", "LIT", "A", "THIN", "GREEN", "CANDLE", "TO", "MAKE", "YOU", "JEALOUS", "OF", "ME"))
    //val time1 = measureTimeMillis {
    //    SortedSmallestAverageDistance(MemoizedDistanceAlgorithm(LevenshteinDistance())).getWords(testSource, testDictionary)
    //}
    val time2 = measureTimeMillis {
        MemoizedSortedSmallestAverageDistance(LevenshteinDistance()).getWords(testSource, testDictionary)
    }
    //val result = SortedSmallestAverageDistance(MemoizedDistanceAlgorithm(LevenshteinDistance())).getWords(testSource, testDictionary)
    //println("${result.words.subList(0,50)}")
    println("$time2")
}


