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
        return when {
            match(IF) -> ifStatement()
            match(PRINT) -> printStatement()
            match(WHILE) -> whileStatement()
            match(LEFT_BRACE) -> Statement.Block(block())
            else -> expressionStatement()
        }
    }

    private fun whileStatement(): Statement {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after while condition.")
        val body = statement()

        return Statement.While(condition, body)
    }

    private fun ifStatement(): Statement {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        val elseBranch = if (match(ELSE)) statement() else null

        return Statement.If(condition, thenBranch, elseBranch)
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

    private fun block(): List<Statement?> {
        val statements = mutableListOf<Statement?>()

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun assignment(): Expression {
        val expression = or()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expression is Expression.Variable) {
                val name = expression.name
                return Expression.Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expression
    }

    private fun varDeclaration(): Statement {
        val name = consume(IDENTIFIER, "Expect variable name.")
        val initializer = if (match(EQUAL)) expression() else null
        consume(SEMICOLON, "Expect ';' after initializer.")

        return Statement.Var(name, initializer)
    }

    private fun expression(): Expression = assignment()

    private fun parseRecursive(
        matchTokens: List<TokenType>,
        rootExpression: Expression,
        rightLambda: () -> Expression,
        expressionFactory: (root: Expression, previous: Token, right: Expression) -> Expression = binaryExpressionFactory
    ): Expression {
        if (match(*matchTokens.toTypedArray())) {
            val operator = previous()
            val right = rightLambda()
            return parseRecursive(matchTokens, expressionFactory(rootExpression, operator, right), rightLambda)
        }
        return rootExpression
    }

    private val logicalExpressionFactory: (Expression, Token, Expression) -> Expression =
        { root: Expression, previous: Token, right: Expression -> Expression.Logical(root, previous, right) }

    private val binaryExpressionFactory: (Expression, Token, Expression) -> Expression =
        { root: Expression, previous: Token, right: Expression -> Expression.Binary(root, previous, right) }

    private fun or(): Expression = parseRecursive(listOf(OR), and(), ::and, logicalExpressionFactory)

    private fun and() = parseRecursive(listOf(AND), equality(), ::equality, logicalExpressionFactory)

    private fun equality() = parseRecursive(listOf(BANG_EQUAL, EQUAL_EQUAL), comparison(), ::comparison)

    private fun comparison() = parseRecursive(listOf(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL), term(), ::term)

    private fun term() = parseRecursive(listOf(MINUS, PLUS), factor(), ::factor)

    private fun factor() = parseRecursive(listOf(SLASH, STAR), unary(), ::unary)

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