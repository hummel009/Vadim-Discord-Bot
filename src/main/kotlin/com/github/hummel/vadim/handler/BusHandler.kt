package com.github.hummel.vadim.handler

import com.github.hummel.vadim.ApiHolder
import com.github.hummel.vadim.bean.BotData
import com.github.hummel.vadim.factory.ServiceFactory
import com.github.hummel.vadim.service.DataService
import com.github.hummel.vadim.utils.decode
import com.github.hummel.vadim.utils.encode
import com.github.hummel.vadim.utils.escapeMarkdownV2
import com.github.hummel.vadim.utils.resizeImage
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.utils.FileProxy
import net.dv8tion.jda.api.utils.FileUpload
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.media.InputMediaAudio
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo
import java.io.File
import java.net.URL
import java.nio.file.Files

object BusHandler : EventListener, LongPollingSingleThreadUpdateConsumer {
	private val dataService: DataService = ServiceFactory.dataService

	override fun onEvent(event: GenericEvent) {
		if (event is MessageReceivedEvent) {
			if (event.author.isBot) {
				return
			}

			val globalData = dataService.loadGlobalData()

			val discordChannelId = event.channel.idLong
			val telegramChatId = globalData.globalBus.find {
				it.discordChannelId == discordChannelId
			}?.telegramChatId ?: return

			Bridge.transferToTelegram(event, telegramChatId)
		}
	}

	override fun consume(update: Update) {
		if (update.hasMessage()) {
			if (update.message.from.isBot) {
				return
			}

			val globalData = dataService.loadGlobalData()

			val telegramChatId = update.message.chatId
			val discordChannelId = globalData.globalBus.find {
				it.telegramChatId == telegramChatId
			}?.discordChannelId ?: return

			Bridge.transferToDiscord(update, discordChannelId)
		}
	}

	object Bridge {
		fun transferToTelegram(event: MessageReceivedEvent, telegramChatId: Long) {
			try {
				val reference = event.message.referencedMessage
				val referenceId = reference?.contentStripped?.takeIf {
					it.contains("औ")
				}?.substringAfter("औ")

				val ownSide = reference != null && referenceId == null

				val message = buildString {
					val content = event.message.contentStripped
					val author = with(event.message.author.effectiveName) {
						replace("  ", " ").replace(" ", "_")
					}
					val answer = if (ownSide) {
						val maxLength = 30
						val originalText = reference.contentStripped
						val displayText = if (originalText.length > maxLength) {
							originalText.take(maxLength) + "..."
						} else {
							originalText
						}
						if (displayText.isEmpty()) {
							""
						} else {
							" ➦ «${displayText}»"
						}
					} else ""
					val id = "\r\n`औ${event.message.idLong.encode()}`"
					val separator = if (content.contains("[\n\r]".toRegex())) "\n\n" else " "

					append("\\#")
					append(author.escapeMarkdownV2())
					append(answer.escapeMarkdownV2())
					append(":")
					append(separator)
					append(content.escapeMarkdownV2())
					append(id)
				}

				ApiHolder.telegram.execute(SendMessage.builder().apply {
					chatId(telegramChatId)
					text(message)
					parseMode(ParseMode.MARKDOWNV2)
					referenceId?.let { replyToMessageId(it.decode().toInt()) }
				}.build())

				val attachments = event.message.attachments
				val stickers = event.message.stickers
				if (attachments.isEmpty() && stickers.isEmpty()) {
					return
				}

				val images = mutableListOf<File>()
				val videos = mutableListOf<File>()
				val audios = mutableListOf<File>()
				val gifs = mutableListOf<File>()
				val documents = mutableListOf<File>()

				val tempDir = Files.createTempDirectory("discord_attachments_")
				val tempFiles = mutableListOf<File>()

				try {
					for (attachment in attachments) {
						if (attachment.size >= 9_999_999) {
							continue
						}
						val byteArray = FileProxy(attachment.proxyUrl).download().join().readBytes()
						val tempFile = tempDir.resolve("${System.currentTimeMillis()}_${attachment.fileName}").toFile()
						tempFile.writeBytes(byteArray)
						tempFiles.add(tempFile)

						when {
							listOf("jpg", "jpeg", "png").any {
								attachment.fileName.lowercase().contains(it)
							} -> images.add(tempFile)

							listOf("mp4", "mov", "mpg", "mpeg").any {
								attachment.fileName.lowercase().contains(it)
							} -> videos.add(tempFile)

							listOf("mp3", "wav", "ogg", "m4a").any {
								attachment.fileName.lowercase().contains(it)
							} -> audios.add(tempFile)

							attachment.fileName.lowercase().contains("gif") -> gifs.add(tempFile)
							else -> documents.add(tempFile)
						}
					}
					for (sticker in stickers) {
						val url = sticker.iconUrl
						if (url.contains(".json")) {
							continue
						}

						val byteArray = URL(url).readBytes()
						val extension = url.substringAfterLast('.', "").lowercase()
						val fileName = "${sticker.id}.$extension"
						val tempFile = tempDir.resolve(fileName).toFile()
						tempFile.writeBytes(byteArray)
						tempFiles.add(tempFile)

						when (extension) {
							"jpg", "jpeg", "png" -> images.add(tempFile)
							"gif" -> gifs.add(tempFile)
						}
					}

					if (images.size > 1) {
						ApiHolder.telegram.execute(SendMediaGroup.builder().apply {
							chatId("$telegramChatId")
							medias(images.map {
								InputMediaPhoto(it, it.name)
							})
						}.build())
					} else if (images.size == 1) {
						ApiHolder.telegram.execute(SendPhoto.builder().apply {
							chatId(telegramChatId)
							photo(InputFile(images[0]))
						}.build())
					}

					if (videos.size > 1) {
						ApiHolder.telegram.execute(SendMediaGroup.builder().apply {
							chatId("$telegramChatId")
							medias(videos.map {
								InputMediaVideo(it, it.name)
							})
						}.build())
					} else if (videos.size == 1) {
						ApiHolder.telegram.execute(SendVideo.builder().apply {
							chatId(telegramChatId)
							video(InputFile(videos[0]))
						}.build())
					}

					if (audios.size > 1) {
						ApiHolder.telegram.execute(SendMediaGroup.builder().apply {
							chatId("$telegramChatId")
							medias(audios.map {
								InputMediaAudio(it, it.name)
							})
						}.build())
					} else if (audios.size == 1) {
						ApiHolder.telegram.execute(SendAudio.builder().apply {
							chatId(telegramChatId)
							audio(InputFile(audios[0]))
						}.build())
					}

					if (documents.size > 1) {
						ApiHolder.telegram.execute(SendMediaGroup.builder().apply {
							chatId("$telegramChatId")
							medias(documents.map {
								InputMediaDocument(it, it.name)
							})
						}.build())
					} else if (documents.size == 1) {
						ApiHolder.telegram.execute(SendDocument.builder().apply {
							chatId(telegramChatId)
							document(InputFile(documents[0]))
						}.build())
					}

					for (inputFile in gifs) {
						ApiHolder.telegram.execute(SendAnimation.builder().apply {
							chatId(telegramChatId)
							animation(InputFile(inputFile))
						}.build())
					}
				} catch (ex: Exception) {
					ex.printStackTrace()
				} finally {
					tempFiles.forEach { it.delete() }
					tempDir.toFile().delete()
				}
			} catch (_: Exception) {
			}
		}

		fun transferToDiscord(update: Update, discordChannelId: Long) {
			try {
				val reply = update.message.replyToMessage
				val replyId = (reply?.text ?: reply?.caption)?.takeIf {
					it.contains("औ")
				}?.substringAfter("औ")

				val ownSide = reply != null && replyId == null

				val message = buildString {
					val content = update.message.text ?: update.message.caption ?: ""
					val author = (update.message.from.userName ?: listOfNotNull(
						update.message.from.firstName, update.message.from.lastName
					).joinToString("_")).replace("\\s+".toRegex(), "_")
					val answer = if (ownSide) {
						val maxLength = 30
						val originalText = reply.text ?: reply.caption ?: ""
						val displayText = if (originalText.length > maxLength) {
							originalText.take(maxLength) + "..."
						} else {
							originalText
						}
						if (displayText.isEmpty()) {
							""
						} else {
							" ➦ «$displayText»"
						}
					} else ""
					val id = "\r\n-# औ${update.message.messageId.toLong().encode()}"
					val separator = if (content.contains("[\n\r]".toRegex())) "\n\n" else " "

					append("__#")
					append(author)
					append("__")
					append(answer)
					append(":")
					append(separator)
					append(content)
					append(id)
				}

				val channel = ApiHolder.discord.getTextChannelById(
					discordChannelId
				) ?: ApiHolder.discord.getThreadChannelById(
					discordChannelId
				) ?: return

				fun sendFile(fileId: String, fileName: String, isImageAndResize: Boolean = false) {
					val url = ApiHolder.telegram.execute(GetFile(fileId)).getFileUrl(BotData.telegramToken)
					val byteArray = URL(url).readBytes()
					val result = if (isImageAndResize) byteArray.resizeImage(160) else byteArray
					channel.sendMessage(message).apply {
						addFiles(FileUpload.fromData(result, fileName))
						replyId?.let { setMessageReference(it.decode()) }
						queue()
					}
				}

				when {
					update.message.photo != null -> {
						val photo = update.message.photo.last()

						if (photo.fileSize <= 9_999_999) {
							sendFile(photo.fileId, "photo.jpg")
						}
					}

					update.message.video != null -> {
						val video = update.message.video

						if (video.fileSize <= 9_999_999) {
							sendFile(video.fileId, video.fileName ?: "video.mp4")
						}
					}

					update.message.audio != null -> {
						val audio = update.message.audio

						if (audio.fileSize <= 9_999_999) {
							sendFile(audio.fileId, audio.fileName ?: "audio.mp3")
						}
					}

					update.message.document != null -> {
						val file = update.message.document

						if (file.fileSize <= 9_999_999) {
							sendFile(file.fileId, file.fileName)
						}
					}

					update.message.animation != null -> {
						val animation = update.message.animation

						if (animation.fileSize <= 9_999_999) {
							sendFile(animation.fileId, animation.fileName ?: "animation.gif")
						}
					}

					update.message.voice != null -> {
						val voice = update.message.voice

						if (voice.fileSize <= 9_999_999) {
							sendFile(voice.fileId, "voice.ogg")
						}
					}

					update.message.sticker != null -> {
						val sticker = update.message.sticker

						if (sticker.fileSize <= 9_999_999) {
							sendFile(sticker.fileId, sticker.fileUniqueId + ".webp", true)
						}
					}

					else -> {
						channel.sendMessage(message).apply {
							replyId?.let { setMessageReference(it.decode()) }
							queue()
						}
					}
				}
			} catch (_: Exception) {
			}
		}
	}
}