package io.github.hummel009.discord.vadim.bus.service.impl

import io.github.hummel009.discord.vadim.ApiHolder
import io.github.hummel009.discord.vadim.bean.GuildData
import io.github.hummel009.discord.vadim.bus.bean.FileWrapper
import io.github.hummel009.discord.vadim.bus.bean.MessageWrapper
import io.github.hummel009.discord.vadim.bus.service.TelegramService
import io.github.hummel009.discord.vadim.bus.utils.FileType
import io.github.hummel009.discord.vadim.dao.FileDao
import io.github.hummel009.discord.vadim.factory.DaoFactory
import io.github.hummel009.discord.vadim.utils.I18n
import io.github.hummel009.discord.vadim.utils.config
import io.github.hummel009.discord.vadim.utils.getMessageChannelById
import net.dv8tion.jda.api.utils.FileUpload
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.net.URI

class TelegramServiceImpl : TelegramService {
	private val fileDao: FileDao = DaoFactory.fileDao

	override fun receive(update: Update): MessageWrapper {
		val fileWrappers = mutableListOf<FileWrapper?>()

		fun downloadWithLimit(id: String, size: Long): ByteArray? {
			val url = ApiHolder.telegram.execute(GetFile(id)).getFileUrl(config.telegramToken)

			return URI(url).takeIf { size <= 9_999_999 }?.toURL()?.readBytes()
		}

		when {
			update.message.photo != null -> {
				val photo = update.message.photo.last()

				val fileBytes = downloadWithLimit(photo.fileId, photo.fileSize.toLong())
				val wrapper = fileBytes?.let {
					FileWrapper(it, "jpg", FileType.PHOTO, update.message.hasMediaSpoiler)
				}
				fileWrappers.add(wrapper)
			}

			update.message.video != null -> {
				val video = update.message.video

				val fileBytes = downloadWithLimit(video.fileId, video.fileSize)
				val wrapper = fileBytes?.let {
					FileWrapper(it, "mp4", FileType.VIDEO, update.message.hasMediaSpoiler)
				}
				fileWrappers.add(wrapper)
			}

			update.message.audio != null -> {
				val audio = update.message.audio

				val fileBytes = downloadWithLimit(audio.fileId, audio.fileSize)
				val wrapper = fileBytes?.let {
					FileWrapper(it, "mp3", FileType.AUDIO, update.message.hasMediaSpoiler)
				}
				fileWrappers.add(wrapper)
			}

			update.message.voice != null -> {
				val voice = update.message.voice

				val fileBytes = downloadWithLimit(voice.fileId, voice.fileSize)
				val wrapper = fileBytes?.let {
					FileWrapper(it, "ogg", FileType.VOICE, update.message.hasMediaSpoiler)
				}
				fileWrappers.add(wrapper)
			}

			update.message.document != null -> {
				val doc = update.message.document

				val fileBytes = downloadWithLimit(doc.fileId, doc.fileSize)
				val wrapper = fileBytes?.let {
					FileWrapper(it, doc.fileName.ext(), FileType.DOC, update.message.hasMediaSpoiler)
				}
				fileWrappers.add(wrapper)
			}
		}

		return MessageWrapper(
			(update.message.from.userName ?: listOfNotNull(
				update.message.from.firstName, update.message.from.lastName
			).joinToString("_")),
			update.message.text ?: update.message.caption ?: "",
			update.message.replyToMessage?.text ?: update.message.replyToMessage?.caption,
			update.message.messageId.toString(),
			fileWrappers
		)
	}

	override fun send(m: MessageWrapper, selfId: Long, otherId: Long, guildData: GuildData) {
		val discordChannel = ApiHolder.discord.getMessageChannelById(otherId) ?: return

		if (!m.isCaption()) {
			val parts = m.textMessage.split()
			parts.forEachIndexed { index, part ->
				discordChannel.sendMessage(part).apply {
					if (index == 0 && m.replyToIdIfOtherSide != null) {
						setMessageReference(m.replyToIdIfOtherSide)
					}
					queue()
				}
			}
		}

		for (fw in m.fileWrappers) {
			when (fw.fileType) {
				FileType.PHOTO, FileType.VIDEO, FileType.AUDIO, FileType.VOICE, FileType.GIF, FileType.DOC -> {
					val filePath = fw.allocateWithPath()
					val file = fileDao.getFile(filePath)

					val fileName = if (fw.isSpoiler == true) "SPOILER_${file.name}" else file.name
					discordChannel.sendFiles(FileUpload.fromData(file, fileName)).setContent(m.textCaption).queue {
						fw.freeWithPath(filePath)
					}
				}

				FileType.NULL -> {
					ApiHolder.telegram.execute(SendMessage.builder().apply {
						chatId(selfId)
						text(I18n.of("file_limit", guildData).s())
					}.build())

					discordChannel.sendMessage(m.textCaption + "\n\n" + I18n.of("file_limit", guildData)).queue()
				}
			}
		}
	}

	private fun String.ext(): String? {
		val i = lastIndexOf('.')

		return if (i != -1 && i < length - 1) substring(i + 1) else null
	}

	private fun String.split(): List<String> {
		if (length <= 1999) {
			return listOf(this)
		}

		val parts = mutableListOf<String>()
		var remaining = this

		while (remaining.length > 1999) {
			val splitIndex = findSplitIndex(remaining, 1999)
			parts.add(remaining.take(splitIndex))
			remaining = remaining.substring(splitIndex).trimStart()
		}

		if (remaining.isNotEmpty()) {
			parts.add(remaining)
		}

		return parts
	}

	private fun findSplitIndex(text: String, maxLength: Int): Int {
		val textToCheck = text.take(maxLength)

		val lastParagraph = textToCheck.lastIndexOf("\n\n")
		if (lastParagraph > 0 && lastParagraph < maxLength - 10) {
			return lastParagraph + 2
		}

		val lastDotSpace = textToCheck.lastIndexOf(". ")
		if (lastDotSpace > 0 && lastDotSpace < maxLength - 5) {
			return lastDotSpace + 2
		}

		val punctuationPattern = "[!?;:] ".toRegex()
		val match = punctuationPattern.findAll(textToCheck).lastOrNull { it.range.last < maxLength - 5 }
		if (match != null) {
			return match.range.last + 1
		}

		val lastSpace = textToCheck.lastIndexOf(' ')
		if (lastSpace > 0 && lastSpace < maxLength - 5) {
			return lastSpace + 1
		}

		return maxLength
	}
}