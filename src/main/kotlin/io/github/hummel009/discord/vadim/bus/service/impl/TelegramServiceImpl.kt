package io.github.hummel009.discord.vadim.bus.service.impl

import io.github.hummel009.discord.vadim.ApiHolder
import io.github.hummel009.discord.vadim.bean.GuildData
import io.github.hummel009.discord.vadim.bus.bean.FileWrapper
import io.github.hummel009.discord.vadim.bus.bean.MessageWrapper
import io.github.hummel009.discord.vadim.bus.service.TelegramService
import io.github.hummel009.discord.vadim.bus.utils.FileType
import io.github.hummel009.discord.vadim.bus.utils.split
import io.github.hummel009.discord.vadim.dao.FileDao
import io.github.hummel009.discord.vadim.factory.DaoFactory
import io.github.hummel009.discord.vadim.utils.I18n
import io.github.hummel009.discord.vadim.utils.config
import io.github.hummel009.discord.vadim.utils.error
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.FileUpload
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.objects.Update
import java.net.URL

class TelegramServiceImpl : TelegramService {
	private val fileDao: FileDao = DaoFactory.fileDao

	override fun receive(update: Update): MessageWrapper {
		val fileWrappers = mutableListOf<FileWrapper?>()

		when {
			update.message.photo != null -> {
				val photo = update.message.photo.last()

				if (photo.fileSize <= 9_999_999) {
					val fileBytes = URL(
						ApiHolder.telegram.execute(
							GetFile(photo.fileId)
						).getFileUrl(config.telegramToken)
					).readBytes()

					fileWrappers.add(
						FileWrapper(fileBytes, "jpg", FileType.PHOTO, update.message.hasMediaSpoiler)
					)
				} else {
					fileWrappers.add(null)
				}
			}

			update.message.video != null -> {
				val video = update.message.video

				if (video.fileSize <= 9_999_999) {
					val fileBytes = URL(
						ApiHolder.telegram.execute(
							GetFile(video.fileId)
						).getFileUrl(config.telegramToken)
					).readBytes()

					fileWrappers.add(
						FileWrapper(fileBytes, "mp4", FileType.VIDEO, update.message.hasMediaSpoiler)
					)
				} else {
					fileWrappers.add(null)
				}
			}

			update.message.audio != null -> {
				val audio = update.message.audio

				if (audio.fileSize <= 9_999_999) {
					val fileBytes = URL(
						ApiHolder.telegram.execute(
							GetFile(audio.fileId)
						).getFileUrl(config.telegramToken)
					).readBytes()

					fileWrappers.add(
						FileWrapper(fileBytes, "mp3", FileType.AUDIO, update.message.hasMediaSpoiler)
					)
				} else {
					fileWrappers.add(null)
				}
			}

			update.message.voice != null -> {
				val voice = update.message.voice

				if (voice.fileSize <= 9_999_999) {
					val fileBytes = URL(
						ApiHolder.telegram.execute(
							GetFile(voice.fileId)
						).getFileUrl(config.telegramToken)
					).readBytes()

					fileWrappers.add(
						FileWrapper(fileBytes, "ogg", FileType.VOICE, update.message.hasMediaSpoiler)
					)
				} else {
					fileWrappers.add(null)
				}
			}

			update.message.document != null -> {
				val doc = update.message.document

				if (doc.fileSize <= 9_999_999) {
					val fileBytes = URL(
						ApiHolder.telegram.execute(
							GetFile(doc.fileId)
						).getFileUrl(config.telegramToken)
					).readBytes()

					fileWrappers.add(
						FileWrapper(fileBytes, doc.fileName.ext(), FileType.DOC, update.message.hasMediaSpoiler)
					)
				} else {
					fileWrappers.add(null)
				}
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

	override fun send(m: MessageWrapper, discordChannelId: Long, guildData: GuildData) {
		val discordChannel = ApiHolder.discord.getTextChannelById(
			discordChannelId
		) ?: ApiHolder.discord.getThreadChannelById(
			discordChannelId
		) ?: return

		val msg = m.textMessage

		if (!m.isCaption()) {
			val parts = msg.split()
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
					val action = discordChannel.sendFiles(FileUpload.fromData(file, fileName))

					if (m.isCaption()) {
						action.setContent(msg)
					}

					action.queue {
						fw.freeWithPath(filePath)
					}
				}

				FileType.NULL -> {
					discordChannel.sendMessageEmbeds(
						EmbedBuilder().error(null, I18n.of("file_limit", guildData))
					).queue()
				}
			}
		}
	}

	private fun String.ext(): String? {
		val i = lastIndexOf('.')

		return if (i != -1 && i < length - 1) substring(i + 1) else null
	}
}