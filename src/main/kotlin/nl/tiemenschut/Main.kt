package nl.tiemenschut

import nl.tiemenschut.lox.Scanner
import java.io.File
import kotlin.system.exitProcess

var hadError: Boolean = false

fun main(args: Array<String>) {
    when (args.size) {
        0 -> runPrompt()
        1 -> {
            runFile(args[0])
            if (hadError) exitProcess(65)
        }
        else -> {
            println("Usage: klox [scriptfile]")
            exitProcess(64)
        }
    }
}

fun runFile(file: String) = run(File(file).readText())

fun runPrompt() {
    while (true) {
        print("> ")
        readlnOrNull()?.let { run(it) } ?: break
        hadError = false
    }
}

fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    tokens.forEach { token ->
        println(token)
    }
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun report(line: Int, where: String, message: String) {
    println("[line $line] Error$where: $message")
    hadError = true
}
