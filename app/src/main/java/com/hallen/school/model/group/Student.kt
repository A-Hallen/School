package com.hallen.school.model.group

data class Student(
    var name: String,
    var movil: String = "",
    var key: String? = null,
    var index: Int? = null,
    var asistance: Map<String, Map<String, Any>> = mapOf()
)
