package io.github.hummel009.discord.vadim.bus.bean

import io.github.hummel009.discord.vadim.bus.utils.FileType
import io.github.hummel009.discord.vadim.bus.utils.decode
import io.github.hummel009.discord.vadim.bus.utils.encode
import io.github.hummel009.discord.vadim.bus.utils.signatureStart

data class MessageWrapper(
	private val authorRaw: String,
	private val textRaw: String,
	private val replyToRaw: String?,
	private val signatureRaw: String,
	private val fileWrappersRaw: List<FileWrapper?>,
) {
	private val author: String = with(authorRaw) {
		replace("  ", " ").replace(" ", "_")
	}

	private val text: String = with(textRaw) {
		escapeMarkdown()
	}

	val replyToQuoteIfSelfSide: String? = replyToRaw?.takeIf {
		!it.contains(signatureStart)
	}?.let { text ->
		val quote = text.replace("\n", " ").take(30).escapeMarkdown().let {
			if (text.length > 30) "$it..." else it
		}
		" ➦ «$quote»:\n"
	}

	val replyToIdIfOtherSide: Long? = replyToRaw?.takeIf {
		it.contains(signatureStart)
	}?.substringAfter(signatureStart)?.decode()

	private val signature: String = run {
		"$signatureStart${signatureRaw.toLong().encode()}"
	}

	private val separator: String = run {
		if (replyToQuoteIfSelfSide != null) {
			"\n"
		} else if ("\n" in textRaw) {
			"\n\n"
		} else {
			" "
		}
	}

	val textMessage: String = run {
		if (isCaption()) {
			"`#${author}:` `${signature}`"
		} else {
			"`#${author}${replyToQuoteIfSelfSide ?: ":"}`${separator}${text}\n\n`${signature}`"
		}
	}

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

	fun isCaption(): Boolean = replyToQuoteIfSelfSide == null && text.isEmpty()

	private fun String.escapeMarkdown(): String {
		val specialChars =
			setOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')

		return map { if (it in specialChars) "\\$it" else it }.joinToString("")
	}
}