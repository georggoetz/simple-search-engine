package search
import java.io.File

enum class SearchStrategy {
    ALL, ANY, NONE
}

class InvertedIndex(rows: List<String>) {
    private val map = HashMap<String, MutableList<Int>>()
    init {
        for ((index, row) in rows.withIndex()) {
            for (word in row.split(" ").map { it.lowercase() }) {
                map.getOrPut(word) { mutableListOf() } += index
            }
        }
    }

    private fun search(word: String): MutableList<Int> = map.getOrDefault(word, mutableListOf())
    fun search(words: List<String>): Map<String, List<Int>> = words.associateWith { this.search(it) }
}

fun checkArgs(args: Array<String>): Boolean = args.size == 2 && args[0].compareTo("--data", true) == 0

fun promptForSearchStrategy(message: String): SearchStrategy {
    while (true) {
        println(message)
        try {
            return SearchStrategy.valueOf(readln().uppercase())
        } catch (_: IllegalArgumentException) {
        }
    }
}

fun main(args: Array<String>) {
    if (!checkArgs(args)) {
        println("usage: search --data <file_name>")
        return
    }
    val people = File(args[1]).readLines()
    val invertedIndex = InvertedIndex(people)
    while (true) {
        println("=== Menu ===")
        println("1. Find a person")
        println("2. Print all people")
        println("0. Exit")
        when (readln().toInt()) {
            1 -> {
                val searchStrategy = promptForSearchStrategy("Select a matching strategy: ALL, ANY, NONE")
                println("Enter a name or email to search all matching people.")
                val words = readln().lowercase().split(" ")
                val resultIndex = invertedIndex.search(words)
                val resultRows = resultIndex.values.flatten().distinct().sorted()
                when (searchStrategy) {
                    SearchStrategy.ALL -> {
                        for (row in resultRows) {
                            if (resultIndex.all { it.value.contains(row) }) {
                                println(people[row])
                            }
                        }
                    }
                    SearchStrategy.ANY -> {
                        for (row in resultRows) {
                            println(people[row])
                        }
                    }
                    SearchStrategy.NONE -> {
                        for ((index, person) in people.withIndex()) {
                            if (index !in resultRows) {
                                println(person)
                            }
                        }
                    }
                }
            }
            2 -> {
                println("=== List of people ===")
                people.forEach(::println)
            }
            0 -> {
                println("Bye!")
                return
            }
            else -> {
                println("Incorrect option! Try again.")
            }
        }
    }
}
