package com.hallen.school.ui.welcome.notas

data class Nota(
    var index: Int,
    var title: String,
    val content: String = "",
    val dibujo: Any? = null
) {
    constructor() : this(0, "", "", null)
}
