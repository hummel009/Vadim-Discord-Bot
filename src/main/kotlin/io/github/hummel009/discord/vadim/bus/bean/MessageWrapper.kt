package io.github.hummel009.discord.vadim.bus.bean

import io.github.hummel009.discord.vadim.bus.utils.FileType

data class MessageWrapper(
	private val authorRaw: String,
	private val textRaw: String,
	private val referenceRaw: String?,
	private val signatureRaw: String,
	private val fileWrappersRaw: List<FileWrapper?>,
) {
	companion object {
		private const val SIGNATURE_START: String = "औ"
		private const val SIGNATURE_ALPHABET: String = "इईउऊऋएऐकखगघङचछजझञटठडढणतदनपफब"
	}

	private val author: String = with(authorRaw) {
		replace("  ", " ").replace(" ", "_")
	}

	private val replyQuote: String? = referenceRaw?.takeIf {
		!it.contains(SIGNATURE_START)
	}?.let { text ->
		val quote = text.replace("\n", " ").take(32).let {
			if (text.length > 32) "$it..." else it
		}
		" ➦ `«$quote»`"
	}

	private val preText: String = run {
		if ("\n" in textRaw || replyQuote !== null && textRaw.length > 128) "\n\n" else " "
	}

	private val text: String = with(textRaw) {
		escapeMarkdown()
	}

	private val postText: String = run {
		if ("\n" in textRaw || replyQuote !== null && textRaw.length > 128) "\n\n" else "\n"
	}

	private val signature: String = run {
		"$SIGNATURE_START${signatureRaw.toLong().encode()}"
	}

	fun isCaption(): Boolean = replyQuote == null && text.isBlank()

	fun asMessage(): String = "`#$author`${replyQuote ?: ""}:$preText$text$postText||$signature||"

	fun asCaption(): String = "`#$author` ||$signature||"

	fun getReplyId(): Long? = referenceRaw?.takeIf {
		it.contains(SIGNATURE_START)
	}?.substringAfter(SIGNATURE_START)?.decode()

	fun getFileWrappers(): List<FileWrapper> = run {
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
				ByteArray(0).inputStream(), null, FileType.NULL, false
			)
			fileWrappersSorted + listOf(theOneNullWrapper)
		} else {
			fileWrappersSorted
		}
	}

	private fun String.escapeMarkdown(): String {
		val specialChars = "_*[]()~`>#+-=|{}.!"
		return map { if (it in specialChars) "\\$it" else it }.joinToString("")
	}

	private fun Long.encode(): String {
		val base = SIGNATURE_ALPHABET.length
		val minus = SIGNATURE_ALPHABET.last()

		if (this == 0L) {
			return "${SIGNATURE_ALPHABET[0]}"
		}
		var number = this
		val negative = number < 0
		if (negative) {
			number = -number
		}

		val sb = StringBuilder()
		while (number > 0) {
			val rem = (number % base).toInt()
			sb.append(SIGNATURE_ALPHABET[rem])
			number /= base
		}
		if (negative) {
			sb.append(minus)
		}
		return sb.reverse().toString()
	}

	private fun String.decode(): Long {
		val base = SIGNATURE_ALPHABET.length
		val minus = SIGNATURE_ALPHABET.last()

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
			val index = SIGNATURE_ALPHABET.indexOf(ch)
			if (index == -1) {
				return 0
			}
			result = result * base + index
		}
		return if (negative) -result else result
	}
}