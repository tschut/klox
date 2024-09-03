package nl.tiemenschut.lox

import nl.tiemenschut.KLox.runtimeError
import nl.tiemenschut.lox.TokenType.*

class RuntimeError(val token: Token, message: String) : RuntimeException(message)

class Interpreter : Expression.Visitor<Any>, Statement.Visitor {
    private val environment = Environment()

    fun interpret(statements: List<Statement>) {
        try {
            statements.forEach { visit(it) }
        } catch (runtimeError: RuntimeError) {
            runtimeError(runtimeError)
        }
    }

    override fun visit(statement: Statement) {
        when (statement) {
            is Statement.Expression -> visit(statement.expression)
            is Statement.Print -> println(visit(statement.expression).stringify())
            is Statement.Var -> environment.define(statement.name.lexeme, statement.initializer?.let { visit(it) })
        }
    }

    override fun visit(expression: Expression): Any? = when (expression) {
        is Expression.Binary -> {
            val right = visit(expression.right)
            val left = visit(expression.left)

            when (expression.operator.type) {
                MINUS -> {
                    checkNumberOperands(expression.operator, left, right)
                    (left as Double) - (right as Double)
                }

                SLASH -> {
                    if (right is Double && right == 0.0) throw RuntimeError(expression.operator, "Division by zero.")
                    checkNumberOperands(expression.operator, left, right)
                    (left as Double) / (right as Double)
                }

                STAR -> {
                    checkNumberOperands(expression.operator, left, right)
                    (left as Double) * (right as Double)
                }

                PLUS -> {
                    if (left is Double && right is Double) (left + right)
                    else if (left is String || right is String) ("${left.stringify()}${right.stringify()}")
                    else throw RuntimeError(
                        expression.operator,
                        "Operands must be two numbers or one of them must be a string."
                    )
                }

                GREATER -> {
                    checkNumberOperands(expression.operator, left, right)
                    (left as Double) > (right as Double)
                }

                GREATER_EQUAL -> {
                    checkNumberOperands(expression.operator, left, right)
                    (left as Double) >= (right as Double)
                }

                LESS -> {
                    checkNumberOperands(expression.operator, left, right)
                    (left as Double) < (right as Double)
                }

                LESS_EQUAL -> {
                    checkNumberOperands(expression.operator, left, right)
                    (left as Double) <= (right as Double)
                }

                BANG_EQUAL -> !isEqual(left, right)
                EQUAL_EQUAL -> isEqual(left, right)
                else -> null
            }
        }

        is Expression.Grouping -> visit(expression.expression)
        is Expression.Literal -> expression.value
        is Expression.Unary -> {
            val right = visit(expression.right)

            when (expression.operator.type) {
                MINUS -> {
                    checkNumberOperands(expression.operator, right)
                    -(right as Double)
                }

                BANG -> !isTruthy(right)
                else -> null
            }
        }

        is Expression.Variable -> environment.get(expression.name)
    }

    private fun checkNumberOperands(operator: Token, vararg right: Any?) {
        if (right.any { it !is Double }) {
            when (right.size) {
                1 -> throw RuntimeError(operator, "Operand must be a number.")
                else -> throw RuntimeError(operator, "Operands must be numbers.")
            }
        }
    }

    private fun isEqual(a: Any?, b: Any?): Boolean = if (a == null) (b == null) else (a == b)

    private fun isTruthy(thing: Any?): Boolean = when (thing) {
        null -> false
        is Boolean -> thing
        else -> true
    }
}

private fun Any?.stringify(): String {
    return if (this == null) "nil"
    else if (this is Double) toString().let { if (it.endsWith(".0")) it.substringBefore('.') else it }
    else toString()
}
