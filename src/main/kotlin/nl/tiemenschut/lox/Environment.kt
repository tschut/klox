package nl.tiemenschut.lox

class Environment(
    private val values: MutableMap<String, Any?> = mutableMapOf()
) {
    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token) = if (values.containsKey(name.lexeme)) {
        values[name.lexeme]
    } else {
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?): Any? {
        if (values.containsKey(name.lexeme)) {
            return values.put(name.lexeme, value)
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}