package nl.tiemenschut.lox

import nl.tiemenschut.KLox
import nl.tiemenschut.lox.TokenType.*

class ParseError : RuntimeException()

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0

    fun parse(): List<Statement> {
        val statements = mutableListOf<Statement>()
        while (!isAtEnd()) {
            declaration()?.let { statements.add(it) }
        }

        return statements
    }

    private fun declaration(): Statement? {
        try {
            return if (match(VAR)) varDeclaration()
            else statement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun statement(): Statement {
        return if (match(PRINT)) printStatement()
        else expressionStatement()
    }

    private fun printStatement(): Statement {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Statement.Print(value)
    }

    private fun expressionStatement(): Statement {
        val expression = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Statement.Expression(expression)
    }

    private fun varDeclaration(): Statement {
        val name = consume(IDENTIFIER, "Expect variable name.")
        val initializer = if (match(EQUAL)) expression() else null
        consume(SEMICOLON, "Expect ';' after initializer.")

        return Statement.Var(name, initializer)
    }

    private fun expression(): Expression = equality()

    private fun parseBinary(
        matchTokens: List<TokenType>,
        rootExpression: Expression,
        rightLambda: () -> Expression,
    ): Expression {
        if (match(*matchTokens.toTypedArray())) {
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
        return if (match(BANG, MINUS)) {
            Expression.Unary(previous(), unary())
        } else {
            primary()
        }
    }

    private fun primary(): Expression {
        return when {
            match(FALSE) -> Expression.Literal(false)
            match(TRUE) -> Expression.Literal(true)
            match(NIL) -> Expression.Literal(null)
            match(NUMBER, STRING) -> Expression.Literal(previous().literal)
            match(IDENTIFIER) -> Expression.Variable(previous())
            match(LEFT_PAREN) -> {
                val expression = expression()
                consume(RIGHT_PAREN, "Expect ')' after expression.")
                Expression.Grouping(expression)
            }

            else -> throw error(peek(), "Expected expression.")
        }
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) {
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
            if (peek().type in listOf(CLASS, FOR, FUN, IF, PRINT, RETURN, VAR, WHILE)) return

            advance()
        }
    }

    private fun match(vararg types: TokenType): Boolean {
        return if (check(*types)) {
            advance()
            true
        } else false
    }

    private fun check(vararg types: TokenType) = if (isAtEnd()) false else peek().type in types

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd() = peek().type == EOF

    private fun peek() = tokens[current]

    private fun previous() = tokens[current - 1]

}