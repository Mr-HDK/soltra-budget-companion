package com.hdk.soltra.util

import java.math.BigDecimal
import java.math.RoundingMode

fun String.amountExpressionToMinorOrNull(): Long? {
    if (isBlank()) return null
    return AmountExpressionParser(trim().replace(',', '.'))
        .parseOrNull()
        ?.takeIf { it > BigDecimal.ZERO }
        ?.movePointRight(2)
        ?.setScale(0, RoundingMode.HALF_UP)
        ?.toLong()
}

fun String.isAmountExpression(): Boolean {
    return any { it in "+-*/()" }
}

private class AmountExpressionParser(
    private val input: String,
) {
    private var index = 0

    fun parseOrNull(): BigDecimal? {
        return runCatching {
            val result = parseExpression()
            skipSpaces()
            if (index != input.length) null else result
        }.getOrNull()
    }

    private fun parseExpression(): BigDecimal {
        var value = parseTerm()
        while (true) {
            skipSpaces()
            value = when (peek()) {
                '+' -> {
                    index += 1
                    value + parseTerm()
                }
                '-' -> {
                    index += 1
                    value - parseTerm()
                }
                else -> return value
            }
        }
    }

    private fun parseTerm(): BigDecimal {
        var value = parseFactor()
        while (true) {
            skipSpaces()
            value = when (peek()) {
                '*' -> {
                    index += 1
                    value * parseFactor()
                }
                '/' -> {
                    index += 1
                    value.divide(parseFactor(), 8, RoundingMode.HALF_UP)
                }
                else -> return value
            }
        }
    }

    private fun parseFactor(): BigDecimal {
        skipSpaces()
        return when (peek()) {
            '+' -> {
                index += 1
                parseFactor()
            }
            '-' -> {
                index += 1
                parseFactor().negate()
            }
            '(' -> {
                index += 1
                val value = parseExpression()
                skipSpaces()
                require(peek() == ')')
                index += 1
                value
            }
            else -> parseNumber()
        }
    }

    private fun parseNumber(): BigDecimal {
        skipSpaces()
        val start = index
        while (index < input.length && (input[index].isDigit() || input[index] == '.')) {
            index += 1
        }
        require(start != index)
        return input.substring(start, index).toBigDecimal()
    }

    private fun skipSpaces() {
        while (index < input.length && input[index].isWhitespace()) {
            index += 1
        }
    }

    private fun peek(): Char? = input.getOrNull(index)
}
