package nl.tiemenschut

import nl.tiemenschut.lox.*
import java.io.File
import kotlin.system.exitProcess

var hadError: Boolean = false
var hadRuntimeError: Boolean = false

fun main(args: Array<String>) {
    when (args.size) {
        0 -> KLox.runPrompt()
        1 -> {
            KLox.runFile(args[0])
            if (hadError) exitProcess(65)
            if (hadRuntimeError) exitProcess(70)
        }
        else -> {
            println("Usage: klox [scriptfile]")
            exitProcess(64)
        }
    }
}

object KLox {
    private val interpreter = Interpreter()

    fun runFile(file: String) = run(File(file).readText())

    fun runPrompt() {
        while (true) {
            print("> ")
            try {
                readlnOrNull()?.let { run(it) } ?: break
            } catch (e: Exception) {

            }
            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()

        val statements = Parser(tokens).parse()

        if (hadError) return

        interpreter.interpret(statements)
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        val where = if (token.type == TokenType.EOF) "at end" else "at '${token.lexeme}'"
        report(token.line, where, message)
    }

    fun runtimeError(error: RuntimeError) {
        System.err.println("[line ${error.token.line}] ${error.message}")
        hadRuntimeError = true
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error $where: $message")
        hadError = true
    }
}