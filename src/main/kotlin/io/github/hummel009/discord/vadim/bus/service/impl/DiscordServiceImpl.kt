package io.github.hummel009.discord.vadim.bus.service.impl

import io.github.hummel009.discord.vadim.ApiHolder
import io.github.hummel009.discord.vadim.bean.GuildData
import io.github.hummel009.discord.vadim.bus.bean.FileWrapper
import io.github.hummel009.discord.vadim.bus.bean.MessageWrapper
import io.github.hummel009.discord.vadim.bus.service.DiscordService
import io.github.hummel009.discord.vadim.bus.utils.FileType
import io.github.hummel009.discord.vadim.dao.FileDao
import io.github.hummel009.discord.vadim.factory.DaoFactory
import io.github.hummel009.discord.vadim.utils.I18n
import io.github.hummel009.discord.vadim.utils.getMessageChannelById
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.objects.InputFile
import java.net.URI

class DiscordServiceImpl : DiscordService {
	private val fileDao: FileDao = DaoFactory.fileDao

	override fun receive(event: MessageReceivedEvent): MessageWrapper {
		val fileWrappers = mutableListOf<FileWrapper?>()

		fun downloadWithLimit(url: String, size: Int): ByteArray? {
			return if (size <= 9_999_999) {
				URI(url).toURL().readBytes()
			} else {
				null
			}
		}

		for (attachment in event.message.attachments) {
			val fileExtension = attachment.fileExtension?.lowercase()

			when {
				listOf("png", "jpg", "jpeg").any {
					fileExtension?.lowercase() == it
				} -> {
					val fileBytes = downloadWithLimit(attachment.proxyUrl, attachment.size)
					val wrapper = fileBytes?.let {
						FileWrapper(it, "jpg", FileType.PHOTO, attachment.isSpoiler)
					}
					fileWrappers.add(wrapper)
				}

				listOf("mp4").any {
					fileExtension?.lowercase() == it
				} -> {
					val fileBytes = downloadWithLimit(attachment.proxyUrl, attachment.size)
					val wrapper = fileBytes?.let {
						FileWrapper(it, "mp4", FileType.VIDEO, attachment.isSpoiler)
					}
					fileWrappers.add(wrapper)
				}

				listOf("mp3").any {
					fileExtension?.lowercase() == it
				} -> {
					val fileBytes = downloadWithLimit(attachment.proxyUrl, attachment.size)
					val wrapper = fileBytes?.let {
						FileWrapper(it, "mp3", FileType.AUDIO, attachment.isSpoiler)
					}
					fileWrappers.add(wrapper)
				}

				listOf("ogg").any {
					fileExtension?.lowercase() == it
				} -> {
					val fileBytes = downloadWithLimit(attachment.proxyUrl, attachment.size)
					val wrapper = fileBytes?.let {
						FileWrapper(it, "ogg", FileType.VOICE, attachment.isSpoiler)
					}
					fileWrappers.add(wrapper)
				}

				listOf("gif").any {
					fileExtension?.lowercase() == it
				} -> {
					val fileBytes = downloadWithLimit(attachment.proxyUrl, attachment.size)
					val wrapper = fileBytes?.let {
						FileWrapper(it, "gif", FileType.GIF, attachment.isSpoiler)
					}
					fileWrappers.add(wrapper)
				}

				else -> {
					val fileBytes = downloadWithLimit(attachment.proxyUrl, attachment.size)
					val wrapper = fileBytes?.let {
						FileWrapper(it, fileExtension, FileType.DOC, attachment.isSpoiler)
					}
					fileWrappers.add(wrapper)
				}
			}
		}

		return MessageWrapper(
			event.message.author.effectiveName,
			event.message.contentStripped,
			event.message.referencedMessage?.contentStripped,
			event.message.id,
			fileWrappers
		)
	}

	override fun send(m: MessageWrapper, selfId: Long, otherId: Long, guildData: GuildData) {
		val discordChannel = ApiHolder.discord.getMessageChannelById(selfId) ?: return

		if (!m.isCaption()) {
			ApiHolder.telegram.execute(SendMessage.builder().apply {
				chatId(otherId)
				text(m.textMessage)
				parseMode(ParseMode.MARKDOWNV2)
				if (m.replyToIdIfOtherSide != null) {
					replyToMessageId(m.replyToIdIfOtherSide.toInt())
				}
			}.build())
		}

		for (fw in m.fileWrappers) {
			when (fw.fileType) {
				FileType.PHOTO -> {
					val filePath = fw.allocateWithPath()
					val file = fileDao.getFile(filePath)

					ApiHolder.telegram.execute(SendPhoto.builder().apply {
						chatId(otherId)
						photo(InputFile(file))
						hasSpoiler(fw.isSpoiler)
						parseMode(ParseMode.MARKDOWNV2)
						caption(m.textCaption)
					}.build())

					fw.freeWithPath(filePath)
				}

				FileType.VIDEO -> {
					val filePath = fw.allocateWithPath()
					val file = fileDao.getFile(filePath)

					ApiHolder.telegram.execute(SendVideo.builder().apply {
						chatId(otherId)
						video(InputFile(file))
						hasSpoiler(fw.isSpoiler)
						parseMode(ParseMode.MARKDOWNV2)
						caption(m.textCaption)
					}.build())

					fw.freeWithPath(filePath)
				}

				FileType.AUDIO -> {
					val filePath = fw.allocateWithPath()
					val file = fileDao.getFile(filePath)

					ApiHolder.telegram.execute(SendAudio.builder().apply {
						chatId(otherId)
						audio(InputFile(file))
						parseMode(ParseMode.MARKDOWNV2)
						caption(m.textCaption)
					}.build())

					fw.freeWithPath(filePath)
				}

				FileType.VOICE -> {
					val filePath = fw.allocateWithPath()
					val file = fileDao.getFile(filePath)

					ApiHolder.telegram.execute(SendVoice.builder().apply {
						chatId(otherId)
						voice(InputFile(file))
						parseMode(ParseMode.MARKDOWNV2)
						caption(m.textCaption)
					}.build())

					fw.freeWithPath(filePath)
				}

				FileType.GIF -> {
					val filePath = fw.allocateWithPath()
					val file = fileDao.getFile(filePath)

					ApiHolder.telegram.execute(SendAnimation.builder().apply {
						chatId(otherId)
						animation(InputFile(file))
						hasSpoiler(fw.isSpoiler)
						parseMode(ParseMode.MARKDOWNV2)
						caption(m.textCaption)
					}.build())

					fw.freeWithPath(filePath)
				}

				FileType.DOC -> {
					val filePath = fw.allocateWithPath()
					val file = fileDao.getFile(filePath)

					ApiHolder.telegram.execute(SendDocument.builder().apply {
						chatId(otherId)
						document(InputFile(file))
						parseMode(ParseMode.MARKDOWNV2)
						caption(m.textCaption)
					}.build())

					fw.freeWithPath(filePath)
				}

				FileType.NULL -> {
					discordChannel.sendMessage(I18n.of("file_limit", guildData).s()).queue()

					ApiHolder.telegram.execute(SendMessage.builder().apply {
						chatId(otherId)
						text(m.textCaption + "\n\n" + I18n.of("file_limit", guildData))
					}.build())
				}
			}
		}
	}
}