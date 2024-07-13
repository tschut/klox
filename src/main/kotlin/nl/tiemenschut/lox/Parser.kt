package nl.tiemenschut.lox

import nl.tiemenschut.lox.TokenType.*

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0

    private fun expression(): Expression = equality()

    private fun parseBinary(matchTokens: List<TokenType>, rootExpression: Expression, rightLambda: () -> Expression): Expression {
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

    private fun factor() = parseBinary(listOf(SLASH, STAR), unary(),::unary)

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
                consume(RIGHT_PAREN, "Expect ')' after expression.") // WIP
                Expression.Grouping(expression)
            }
            else -> throw UnsupportedOperationException("wtf")
        }
    }

    private fun match(types: List<TokenType>): Boolean {
        if (check(types)) {
            advance()
            return true
        }
        return false
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