package nl.tiemenschut.lox

import nl.tiemenschut.lox.Expression as LoxExpression

sealed class Statement {
    interface Visitor {
        fun visit(statement: Statement)
    }

    data class Block(
        val statements: List<Statement?>
    ) : Statement()

    data class Expression(
        val expression: LoxExpression
    ) : Statement()

    data class If(
        val condition: LoxExpression,
        val thenBranch: Statement,
        val elseBranch: Statement?
    ) : Statement()

    data class Print(
        val expression: LoxExpression
    ) : Statement()

    data class While(
        val condition: LoxExpression,
        val body: Statement
    ) : Statement()

    data class Var(
        val name: Token,
        val initializer: LoxExpression?
    ) : Statement()
}