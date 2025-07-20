package com.github.hummel.nikanor.handler

import com.github.hummel.nikanor.ApiHolder
import com.github.hummel.nikanor.factory.ServiceFactory
import com.github.hummel.nikanor.service.DataService
import com.github.hummel.nikanor.utils.getDiscordAuthorName
import com.github.hummel.nikanor.utils.getTelegramAuthorName
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

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

			val content = event.message.contentDisplay
			val multiline = content.contains("\n") || content.contains("\r")
			val author = getDiscordAuthorName(event.message)

			val separator = if (multiline) "\n\n" else " "

			ApiHolder.telegram.execute(SendMessage.builder().apply {
				chatId(telegramChatId)
				text("@$author:$separator$content")
			}.build())
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
			val multiline = content.contains("\n") || content.contains("\r")
			val author = getTelegramAuthorName(update.message)

			val separator = if (multiline) "\n\n" else " "

			val channel = ApiHolder.discord.getTextChannelById(
				discordChannelId
			) ?: ApiHolder.discord.getThreadChannelById(
				discordChannelId
			)

			channel?.let { channel ->
				val message = "__@${author}__:$separator$content"
				channel.sendMessage(message).queue()
			}
		}
	}
}