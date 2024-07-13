package nl.tiemenschut

import nl.tiemenschut.lox.*
import java.io.File
import kotlin.system.exitProcess

var hadError: Boolean = false

fun main(args: Array<String>) {
    when (args.size) {
        0 -> KLox.runPrompt()
        1 -> {
            KLox.runFile(args[0])
            if (hadError) exitProcess(65)
        }
        else -> {
            println("Usage: klox [scriptfile]")
            exitProcess(64)
        }
    }
}

object KLox {
    fun runFile(file: String) = run(File(file).readText())

    fun runPrompt() {
        while (true) {
            print("> ")
            readlnOrNull()?.let { run(it) } ?: break
            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()

        val expression = Parser(tokens).parse()

        if (hadError || expression == null) return

        AstPrinter().print(expression)
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        val where = if (token.type == TokenType.EOF) "at end" else "at '${token.lexeme}'"
        report(token.line, where, message)
    }

    private fun report(line: Int, where: String, message: String) {
        println("[line $line] Error $where: $message")
        hadError = true
    }
}