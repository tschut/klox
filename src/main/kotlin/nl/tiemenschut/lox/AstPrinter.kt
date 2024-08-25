package nl.tiemenschut.lox

class AstPrinter : Expression.Visitor<String> {
    fun print(expression: Expression) {
        println(expression.visit(this))
    }

    override fun visit(expression: Expression): String = when (expression) {
        is Expression.Binary -> parenthesize(expression.operator.lexeme, expression.left, expression.right)
        is Expression.Grouping -> parenthesize("group", expression.expression)
        is Expression.Literal -> expression.value?.toString() ?: "nil"
        is Expression.Unary -> parenthesize(expression.operator.lexeme, expression.right)
    }

    private fun parenthesize(name: String, vararg expressions: Expression): String {
        return expressions.joinToString(prefix = "($name ", postfix = ")", separator = " ") { expression ->
            expression.visit(this)!!
        }
    }
}