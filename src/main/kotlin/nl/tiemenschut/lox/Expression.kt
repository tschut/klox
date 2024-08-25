package nl.tiemenschut.lox

sealed class Expression {
    interface Visitor<R> {
        fun visit(expression: Expression): R?
    }

    data class Binary(val left: Expression, val operator: Token, val right: Expression) : Expression()
    data class Grouping(val expression: Expression) : Expression()
    data class Literal(val value: Any?) : Expression()
    data class Unary(val operator: Token, val right: Expression) : Expression()
}