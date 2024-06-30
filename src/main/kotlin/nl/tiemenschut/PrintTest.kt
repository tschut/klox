package nl.tiemenschut

import nl.tiemenschut.lox.AstPrinter
import nl.tiemenschut.lox.Expression
import nl.tiemenschut.lox.Token
import nl.tiemenschut.lox.TokenType

fun main() {
    Expression.Binary(
        Expression.Unary(
            Token(TokenType.MINUS, "-", null, 1),
            Expression.Literal(123)
        ),
        Token(TokenType.STAR, "*", null, 1),
        Expression.Grouping(Expression.Literal(45.67))
    ).also { AstPrinter().print(it) }
}