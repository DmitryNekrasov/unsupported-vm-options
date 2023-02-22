import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.nio.file.Path

fun generate(path: String): Set<String> {
    val cmd = listOf(Path.of(path, "bin", "java").toString(), "-XX:+PrintFlagsFinal", "-version")
    val process = ProcessBuilder().command(cmd).start()
    val lines = process.inputStream.reader().use { it.readLines() }
    val status = process.waitFor()
    if (status != 0) {
        throw RuntimeException("starting java failed")
    }
    return lines.slice(1..lines.lastIndex).map { it.split(' ').filter { s -> s.isNotEmpty() }[1] }.toSet()
}

fun getNewOptions(old: Set<String>, new: Set<String>): List<String> {
    val ret = mutableListOf<String>()
    for (option in new) {
        if (option !in old) {
            ret.add(option)
        }
    }
    return ret
}

fun List<String>.generateNeedToAdd(): List<String> {
    return this.map { "  {\"$it\", NULL, 0}," }
}

fun generateCurrentUnsupportedOptionsList(argumentsCpp: String): List<String> {
    return argumentsCpp.substringAfter("static optionAttr_t UnsupportedOracleOptions[] = {").substringBefore("};").split("\n")
}

fun main(args: Array<String>) {
    if (args.size < 2) {
        throw IllegalArgumentException("Not enough arguments for the application to work. You need to pass 2 paths to the JDK.")
    }
    if (args.size < 3) {
        throw IllegalArgumentException("Not enough arguments for the application to work. You need to pass a path to the hotspot folder.")
    }
    val pathToJdkOld = args[0]
    val pathToJdkNew = args[1]
    val pathToHotspot = args[2]

    fun List<String>.filterByGlobalsHpp(): List<String> {
        val pathToGlobalsHpp = Path.of(pathToHotspot, "src", "share", "runtime", "globals.hpp").toString()
        val globalsHpp = FileReader(File(pathToGlobalsHpp)).readText()
        return this.filter { !globalsHpp.contains(it) }
    }

    val pathToArgumentsCpp = Path.of(pathToHotspot, "src", "share", "runtime", "arguments.cpp").toString()
    val argumentsCpp = FileReader(File(pathToArgumentsCpp)).readText()

    fun List<String>.filterByArgumentsCpp(): List<String> {
        return this.filter { !argumentsCpp.contains(it) }
    }

    val needToAdd = getNewOptions(generate(pathToJdkOld), generate(pathToJdkNew)).filterByGlobalsHpp().filterByArgumentsCpp().generateNeedToAdd()
    println("Options number: ${needToAdd.size}")
    println(needToAdd.joinToString("\n"))
    val input = generateCurrentUnsupportedOptionsList(argumentsCpp)
    println(input)
}