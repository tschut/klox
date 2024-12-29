package nl.tiemenschut.lox

class Environment(
    private val values: MutableMap<String, Any?> = mutableMapOf(),
    private val enclosing: Environment? = null
) {
    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? = if (values.containsKey(name.lexeme)) {
        values[name.lexeme]
    } else if (enclosing != null) {
        enclosing.get(name)
    } else {
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?): Any? {
        if (values.containsKey(name.lexeme)) {
            return values.put(name.lexeme, value)
        } else if (enclosing != null) {
            return enclosing.assign(name, value)
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}