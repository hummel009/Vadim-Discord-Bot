package io.github.hummel009.discord.vadim.bus.bean

import io.github.hummel009.discord.vadim.bus.utils.FileType

data class MessageWrapper(
	private val authorRaw: String,
	private val textRaw: String,
	private val replyToRaw: String?,
	private val signatureRaw: String,
	private val fileWrappersRaw: List<FileWrapper?>,
) {
	private val signatureStart: String = "औ"
	private val signatureAlphabet: String = "इईउऊऋएऐकखगघङचछजझञटठडढणतदनपफब"

	private val author: String = with(authorRaw) {
		replace("  ", " ").replace(" ", "_")
	}

	private val text: String = with(textRaw) {
		escapeMarkdown()
	}

	val replyToQuoteIfSelfSide: String? = replyToRaw?.takeIf {
		!it.contains(signatureStart)
	}?.let { text ->
		val quote = text.replace("\n", " ").take(32).let {
			if (text.length > 32) "$it..." else it
		}
		" ➦ `«$quote»`:"
	}

	val replyToIdIfOtherSide: Long? = replyToRaw?.takeIf {
		it.contains(signatureStart)
	}?.substringAfter(signatureStart)?.decode()

	private val signature: String = run {
		"$signatureStart${signatureRaw.toLong().encode()}"
	}

	private val separator: String = run {
		if ("\n" in textRaw || replyToQuoteIfSelfSide !== null && textRaw.length > 128) "\n\n" else " "
	}

	val textMessage: String = "`#${author}`${replyToQuoteIfSelfSide ?: ":"}${separator}${text}\n\n||${signature}||"

	val textCaption: String = "`#${author}` ||${signature}||"

	val fileWrappers: List<FileWrapper> = run {
		val fileWrappersSorted = fileWrappersRaw.filterNotNull().sortedBy {
			when (it.fileType) {
				FileType.PHOTO -> 0
				FileType.VIDEO -> 1
				FileType.GIF -> 2
				FileType.DOC -> 3
				FileType.AUDIO -> 4
				FileType.VOICE -> 5
				FileType.NULL -> throw Exception("Null must not appear here")
			}
		}

		val hasNulls = fileWrappersRaw.any { it == null }

		if (hasNulls) {
			val theOneNullWrapper = FileWrapper(
				ByteArray(0), null, FileType.NULL, false
			)
			fileWrappersSorted + listOf(theOneNullWrapper)
		} else {
			fileWrappersSorted
		}
	}

	fun isCaption(): Boolean = replyToQuoteIfSelfSide == null && text.isBlank()

	private fun String.escapeMarkdown(): String {
		val specialChars = "_*[]()~`>#+-=|{}.!"
		return map { if (it in specialChars) "\\$it" else it }.joinToString("")
	}

	private fun Long.encode(): String {
		val base = signatureAlphabet.length
		val minus = signatureAlphabet.last()

		if (this == 0L) {
			return "${signatureAlphabet[0]}"
		}
		var number = this
		val negative = number < 0
		if (negative) {
			number = -number
		}

		val sb = StringBuilder()
		while (number > 0) {
			val rem = (number % base).toInt()
			sb.append(signatureAlphabet[rem])
			number /= base
		}
		if (negative) {
			sb.append(minus)
		}
		return sb.reverse().toString()
	}

	private fun String.decode(): Long {
		val base = signatureAlphabet.length
		val minus = signatureAlphabet.last()

		if (isEmpty()) {
			return 0
		}
		var negative = false
		var str = this
		if (str.first() == minus) {
			negative = true
			str = str.drop(1)
		}

		var result = 0L
		for (ch in str) {
			val index = signatureAlphabet.indexOf(ch)
			if (index == -1) {
				return 0
			}
			result = result * base + index
		}
		return if (negative) -result else result
	}
}