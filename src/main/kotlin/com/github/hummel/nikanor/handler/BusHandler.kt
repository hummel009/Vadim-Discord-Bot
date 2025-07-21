package com.github.hummel.nikanor.handler

import com.github.hummel.nikanor.ApiHolder
import com.github.hummel.nikanor.factory.ServiceFactory
import com.github.hummel.nikanor.service.DataService
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.utils.FileProxy
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.media.InputMedia
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import java.io.File
import java.nio.file.Files

object BusHandler : EventListener, LongPollingSingleThreadUpdateConsumer {
	private val dataService: DataService = ServiceFactory.dataService

	// DISCORD
	override fun onEvent(event: GenericEvent) {
		if (event is MessageReceivedEvent) {
			if (event.author.isBot) {
				return
			}

			val discordChannelId = event.channel.idLong
			val busRegistry = dataService.loadBusRegistry()
			if (!busRegistry.discordBus.contains(discordChannelId)) {
				return
			}
			val telegramChatId = busRegistry.discordBus[discordChannelId]!!

			val author = with(event.message.author.effectiveName) {
				replace("  ", " ").replace(" ", "_")
			}

			if (DiscordBridge.tryForwardImageGroupWithText(event, author, telegramChatId)) {
				return
			}

			if (DiscordBridge.tryForwardImageWithText(event, author, telegramChatId)) {
				return
			}

			if (DiscordBridge.tryForwardText(event, author, telegramChatId)) {
				return
			}
		}
	}

	// TELEGRAM
	override fun consume(update: Update) {
		if (update.hasMessage() && update.message.hasText()) {
			if (update.message.from.isBot) {
				return
			}

			val telegramChatId = update.message.chatId
			val busRegistry = dataService.loadBusRegistry()
			if (!busRegistry.telegramBus.contains(telegramChatId)) {
				return
			}
			val discordChannelId = busRegistry.telegramBus[telegramChatId]!!

			val content = update.message.text
			val author = with(update.message.from) {
				(userName ?: listOfNotNull(firstName, lastName).joinToString("_")).replace("  ", " ").replace(" ", "_")
			}
			val isMultiline = content.contains("\n") || content.contains("\r")

			val separator = if (isMultiline) "\n\n" else " "

			val channel = ApiHolder.discord.getTextChannelById(
				discordChannelId
			) ?: ApiHolder.discord.getThreadChannelById(
				discordChannelId
			)

			channel?.let { channel ->
				val message = "__#${author}__:$separator$content"
				channel.sendMessage(message).queue()
			}
		}
	}

	object DiscordBridge {
		fun tryForwardImageGroupWithText(
			event: MessageReceivedEvent, author: String, telegramChatId: Long
		): Boolean {
			try {
				val content = event.message.contentDisplay
				val isMultiline = content.contains("\n") || content.contains("\r")

				val separator = if (isMultiline) "\n\n" else " "

				val attachments = event.message.attachments
				if (attachments.isEmpty()) {
					return false
				}

				val imageAttachments = attachments.filter { it.isImage }
				if (imageAttachments.size < 2) {
					return false
				}

				val medias = mutableListOf<InputMedia>()
				val tempFiles = mutableListOf<File>()

				try {
					for ((i, imageAttachment) in imageAttachments.withIndex()) {
						val byteArray = FileProxy(imageAttachment.url).download().join().readBytes()
						val tempFile = Files.createTempFile("telegram_photo_", imageAttachment.fileExtension).toFile()
						tempFile.writeBytes(byteArray)
						tempFiles.add(tempFile)

						val inputFile = InputMediaPhoto(tempFile, tempFile.name)
						if (i == 0) {
							inputFile.caption = "#$author:$separator$content"
						}
						medias.add(inputFile)
					}

					val sendFunc = SendMediaGroup.builder().apply {
						chatId(telegramChatId.toString())
						medias(medias)
					}.build()

					ApiHolder.telegram.execute(sendFunc)
				} catch (ex: Exception) {
					ex.printStackTrace()
				} finally {
					tempFiles.forEach { it.delete() }
				}
				return true
			} catch (e: Exception) {
				e.printStackTrace()

				return false
			}
		}

		fun tryForwardImageWithText(
			event: MessageReceivedEvent, author: String, telegramChatId: Long
		): Boolean {
			try {
				val content = event.message.contentDisplay
				val isMultiline = content.contains("\n") || content.contains("\r")

				val separator = if (isMultiline) "\n\n" else " "

				val attachments = event.message.attachments
				if (attachments.isEmpty()) {
					return false
				}

				val imageAttachments = attachments.filter { it.isImage }
				if (imageAttachments.size != 1) {
					return false
				}

				val tempFile = Files.createTempFile("telegram_photo_", imageAttachments[0].fileExtension).toFile()

				try {
					val byteArray = FileProxy(imageAttachments[0].url).download().join().readBytes()
					tempFile.writeBytes(byteArray)

					val photo = InputFile(tempFile)

					val sendFunc = SendPhoto.builder().apply {
						chatId(telegramChatId)
						photo(photo)
						caption("#$author:$separator$content")
					}.build()

					ApiHolder.telegram.execute(sendFunc)
				} catch (e: Exception) {
					e.printStackTrace()

					return false
				} finally {
					tempFile.delete()
				}
				return true
			} catch (e: Exception) {
				e.printStackTrace()

				return false
			}
		}

		fun tryForwardText(
			event: MessageReceivedEvent, author: String, telegramChatId: Long
		): Boolean {
			try {
				val content = event.message.contentDisplay
				val isMultiline = content.contains("\n") || content.contains("\r")

				val separator = if (isMultiline) "\n\n" else " "

				val sendFunc = SendMessage.builder().apply {
					chatId(telegramChatId)
					text("#$author:$separator$content")
				}.build()

				ApiHolder.telegram.execute(sendFunc)

				return true
			} catch (e: Exception) {
				e.printStackTrace()

				return false
			}
		}
	}
}