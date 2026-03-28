package io.github.hummel009.discord.vadim.utils

val alphabet: List<Char> = listOf(
	'इ',
	'ई',
	'उ',
	'ऊ',
	'ऋ',
	'ए',
	'ऐ',
	'क',
	'ख',
	'ग',
	'घ',
	'ङ',
	'च',
	'छ',
	'ज',
	'झ',
	'ञ',
	'ट',
	'ठ',
	'ड',
	'ढ',
	'ण',
	'त',
	'द',
	'न',
	'प',
	'फ',
	'ब'
)

val base: Int = alphabet.size
val minus: Char = alphabet.last()

fun Long.encode(): String {
	if (this == 0L) {
		return "${alphabet[0]}"
	}
	var number = this
	val negative = number < 0
	if (negative) {
		number = -number
	}

	val sb = StringBuilder()
	while (number > 0) {
		val rem = (number % base).toInt()
		sb.append(alphabet[rem])
		number /= base
	}
	if (negative) {
		sb.append(minus)
	}
	return sb.reverse().toString()
}

fun String.decode(): Long {
	if (isEmpty()) {
		throw Exception()
	}
	var negative = false
	var str = this
	if (str.first() == minus) {
		negative = true
		str = str.drop(1)
	}

	var result = 0L
	for (ch in str) {
		val index = alphabet.indexOf(ch)
		if (index == -1) {
			throw Exception()
		}
		result = result * base + index
	}
	return if (negative) -result else result
}