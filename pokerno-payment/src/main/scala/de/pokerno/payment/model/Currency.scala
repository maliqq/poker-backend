package de.pokerno.payment.model

sealed class Currency(
    var id: Long,
    var code: String
) {
}
