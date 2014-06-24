package de.pokerno.finance.model

sealed class Currency(
    var id: Long,
    var code: String
) {
}
