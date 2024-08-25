package nl.tiemenschut.lox

sealed class Statement {
    interface Visitor {
        fun visit(statement: Statement)
    }

    data class Expression(val expression: nl.tiemenschut.lox.Expression): Statement()
    data class Print(val expression: nl.tiemenschut.lox.Expression): Statement()
}