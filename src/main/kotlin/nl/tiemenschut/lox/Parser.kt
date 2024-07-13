package nl.tiemenschut.lox

import nl.tiemenschut.KLox
import nl.tiemenschut.lox.TokenType.*

class ParseError : RuntimeException()

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0

    fun parse(): Expression? = try {
        expression()
    } catch (e: ParseError) {
        null
    }

    private fun expression(): Expression = equality()

    private fun parseBinary(
        matchTokens: List<TokenType>,
        rootExpression: Expression,
        rightLambda: () -> Expression,
    ): Expression {
        if (match(matchTokens)) {
            val operator = previous()
            val right = rightLambda()
            return parseBinary(matchTokens, Expression.Binary(rootExpression, operator, right), rightLambda)
        }
        return rootExpression
    }

    private fun equality() = parseBinary(listOf(BANG_EQUAL, EQUAL_EQUAL), comparison(), ::comparison)

    private fun comparison() = parseBinary(listOf(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL), term(), ::term)

    private fun term() = parseBinary(listOf(MINUS, PLUS), factor(), ::factor)

    private fun factor() = parseBinary(listOf(SLASH, STAR), unary(), ::unary)

    private fun unary(): Expression {
        return if (match(listOf(BANG, MINUS))) {
            Expression.Unary(previous(), unary())
        } else {
            primary()
        }
    }

    private fun primary(): Expression {
        return when {
            match(listOf(FALSE)) -> Expression.Literal(false)
            match(listOf(TRUE)) -> Expression.Literal(true)
            match(listOf(NIL)) -> Expression.Literal(null)
            match(listOf(NUMBER, STRING)) -> Expression.Literal(previous().literal)
            match(listOf(LEFT_PAREN)) -> {
                val expression = expression()
                consume(RIGHT_PAREN, "Expect ')' after expression.")
                Expression.Grouping(expression)
            }

            else -> throw error(peek(), "Expected expression.")
        }
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(listOf(type))) {
            return advance()
        }
        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        KLox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return
            if(peek().type in listOf(CLASS, FOR, FUN, IF, PRINT, RETURN, VAR, WHILE))  return

            advance()
        }
    }

    private fun match(types: List<TokenType>): Boolean {
        return if (check(types)) {
            advance()
            true
        } else false
    }

    private fun check(types: List<TokenType>) = if (isAtEnd()) false else peek().type in types

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd() = peek().type == EOF

    private fun peek() = tokens[current]

    private fun previous() = tokens[current - 1]

}