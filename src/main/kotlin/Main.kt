import java.lang.RuntimeException
import java.nio.file.Path

class PrintFlagsFinalGenerator {
    fun generate(path: String): Set<String> {
        val java = Path.of(path, "bin", "java").toString()
        val cmd = listOf(java, "-XX:+PrintFlagsFinal", "-version")
        val process = ProcessBuilder().command(cmd).start()
        val lines = process.inputStream.reader().use { it.readLines() }
        val options = lines.slice(1..lines.lastIndex).map { it.split(' ').filter { s -> s.isNotEmpty() }[1] }.toSet()
        val status = process.waitFor()
        if (status != 0) {
            throw RuntimeException("starting java failed")
        }
        return options
    }
}

fun main(args: Array<String>) {
    if (args.size < 2) {
        throw IllegalArgumentException("Not enough arguments for the application to work. You need to pass 2 paths to the JDK.")
    }
    val pathToJdkOld = args[0]
    val pathToJdkNew = args[1]
    val pffGenerator = PrintFlagsFinalGenerator()
    val jdkOldOptions = pffGenerator.generate(pathToJdkOld)
    val jdkNewOptions = pffGenerator.generate(pathToJdkNew)
    println(jdkOldOptions.joinToString("\n"))
}