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
}