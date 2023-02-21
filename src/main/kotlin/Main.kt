import kotlin.io.path.Path

fun main(args: Array<String>) {
    if (args.size < 2) {
        throw IllegalArgumentException("Not enough arguments for the application to work. You need to pass 2 paths to the JDK.")
    }
    val pathToJdkOld = Path(args[0])
    val pathToJdkNew = Path(args[1])
    println("Path to JDK old: $pathToJdkOld")
    println("Path to JDK new: $pathToJdkNew")
}