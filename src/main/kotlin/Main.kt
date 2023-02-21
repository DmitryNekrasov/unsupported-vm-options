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

fun main(args: Array<String>) {
    if (args.size < 2) {
        throw IllegalArgumentException("Not enough arguments for the application to work. You need to pass 2 paths to the JDK.")
    }
    val pathToJdkOld = args[0]
    val pathToJdkNew = args[1]
    val newOptions = getNewOptions(generate(pathToJdkOld), generate(pathToJdkNew))
    println("New options number: ${newOptions.size}")
    println(newOptions.joinToString("\n"))
}