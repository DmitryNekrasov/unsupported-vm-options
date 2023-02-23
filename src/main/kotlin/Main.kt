import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
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

fun generateCurrentUnsupportedOptionsList(toReplace: String): List<String> {
    return toReplace.split("\n")
}

fun merge(input: List<String>, needToAdd: List<String>): List<String> {
    var p1 = 0
    var p2 = 0
    val result = mutableListOf<String>()
    while (p1 < input.size) {
        val s1 = input[p1]
        if (!s1.startsWith("  {") || p2 >= needToAdd.size) {
            result.add(s1)
            p1++
            continue
        }
        val s2 = needToAdd[p2]
        if (s1 < s2) {
            result.add(s1)
            p1++
        } else {
            result.add(s2)
            p2++
        }
    }
    return result
}

fun addUnsupportedVmOptions(pathToJdkOld: String, pathToJdkNew: String, pathToHotspot: String) {
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

    val toReplace = argumentsCpp.substringAfter("static optionAttr_t UnsupportedOracleOptions[] = {").substringBefore("};")
    val input = generateCurrentUnsupportedOptionsList(toReplace)
    val needToAdd = getNewOptions(generate(pathToJdkOld), generate(pathToJdkNew)).filterByGlobalsHpp().filterByArgumentsCpp().generateNeedToAdd()
    val afterMerge = merge(input, needToAdd).joinToString("\n")
    val newArgumentsCpp = argumentsCpp.replace(toReplace, afterMerge)
    File(pathToArgumentsCpp).outputStream().write(newArgumentsCpp.toByteArray())
    println("Options added successfully")
}

fun main(args: Array<String>) {
    if (args.size < 2) {
        throw IllegalArgumentException("Not enough arguments for the application to work. You need to pass 2 paths to the JDK.")
    }
    if (args.size < 3) {
        throw IllegalArgumentException("Not enough arguments for the application to work. You need to pass a path to the hotspot folder.")
    }
    addUnsupportedVmOptions(args[0], args[1], args[2])
}