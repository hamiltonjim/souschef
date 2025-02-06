package xyz.jimh.souschef.config

enum class UnitPreference(private val label: String) {
    INTERNATIONAL("International"),
    ENGLISH("English"),
    ANY("Any");

    fun getLabel(): String {
        return label
    }
}