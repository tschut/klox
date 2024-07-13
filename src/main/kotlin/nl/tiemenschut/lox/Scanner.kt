package nl.tiemenschut.lox

import nl.tiemenschut.KLox
import nl.tiemenschut.lox.TokenType.*

private val digits = ('0'..'9').toList()
private val alphas = ('a'..'z').toList() + ('A'..'Z').toList() + '_'
private val keywords = mapOf(
    "and" to AND,
    "class" to CLASS,
    "else" to ELSE,
    "false" to FALSE,
    "for" to FOR,
    "fun" to FUN,
    "if" to IF,
    "nil" to NIL,
    "or" to OR,
    "print" to PRINT,
    "return" to RETURN,
    "super" to SUPER,
    "this" to THIS,
    "true" to TRUE,
    "var" to VAR,
    "while" to WHILE,
)

class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd() = current >= source.length

    private fun scanToken() {
        when(val c = advance()) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> {
                if (match('/')) {
                    while(peek() != '\n' && !isAtEnd()) advance()
                } else addToken(SLASH)
            }
            '\n' -> line++
            in listOf(' ', '\r', '\t') -> {} // ignore whitespace
            '"' -> string()
            in digits -> number()
            in alphas -> identifier()
            else -> KLox.error(line, "Unexpected character '$c'")
        }
    }

    private fun identifier() {
        while (peek() in (digits + alphas)) {
            advance()
        }

        val text = source.substring(start, current)
        addToken(keywords[text] ?: IDENTIFIER)
    }

    private fun number() {
        while (peek() in '0'..'9') advance()

        if (peek() == '.' && peekNext().isDigit()) {
            advance()
        }

        while (peek() in '0'..'9') advance()

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            KLox.error(line, "Unterminated string")
            return
        }

        advance()

        addToken(STRING, source.substring(start + 1, current - 1))
    }

    private fun peek(): Char = if (isAtEnd()) Char.MIN_VALUE else source[current]

    private fun peekNext(): Char = if (current + 1 >= source.length) Char.MIN_VALUE else source[current + 1]

    private fun match(expected: Char): Boolean {
        if (peek() != expected) return false

        current++
        return true
    }

    private fun advance() = source[current++]

    private fun addToken(tokenType: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(tokenType, text, literal, line))
    }
}
