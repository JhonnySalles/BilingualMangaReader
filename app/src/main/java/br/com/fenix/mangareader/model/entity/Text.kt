package br.com.fenix.mangareader.model.entity

data class Text(
    val text: String,
    val sequence: Int,
    val x1: Int,
    val y1: Int,
    val x2: Int,
    var y2: Int
)