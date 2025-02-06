package xyz.jimh.souschef.config

interface Listener {
    fun listen(name: String, value: String): Unit
}