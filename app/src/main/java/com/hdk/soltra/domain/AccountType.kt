package com.hdk.soltra.domain

enum class AccountType(val label: String) {
    CASH("Cash (incl. argent cache)"),
    BANK("Banque"),
    CARD("Carte"),
    EWALLET("E-Wallet (incl. crypto)"),
    OTHER("Autre"),
}
