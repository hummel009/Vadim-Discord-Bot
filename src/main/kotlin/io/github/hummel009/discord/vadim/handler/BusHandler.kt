package io.github.hummel009.discord.vadim.handler

import io.github.hummel009.discord.vadim.ApiHolder
import io.github.hummel009.discord.vadim.bus.service.DiscordService
import io.github.hummel009.discord.vadim.bus.service.TelegramService
import io.github.hummel009.discord.vadim.factory.ServiceFactory
import io.github.hummel009.discord.vadim.service.DataService
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.objects.Update

object BusHandler : EventListener, LongPollingSingleThreadUpdateConsumer {
	private val dataService: DataService = ServiceFactory.dataService

	private val discordService: DiscordService = ServiceFactory.discordService
	private val telegramService: TelegramService = ServiceFactory.telegramService

	override fun onEvent(event: GenericEvent) {
		if (event is MessageReceivedEvent) {
			if (event.author.idLong == ApiHolder.discord.selfUser.idLong) {
				return
			}

			val globalData = dataService.loadGlobalData()

			val discordChannelId = event.channel.idLong
			val telegramChatId = globalData.globalBus.find {
				it.discordChannelId == discordChannelId
			}?.telegramChatId ?: return

			val guildData = dataService.loadGuildData(event.guild)

			val messageWrapper = discordService.receive(event)
			discordService.send(messageWrapper, telegramChatId, guildData)
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

			val discordChannel = ApiHolder.discord.getTextChannelById(
				discordChannelId
			) ?: ApiHolder.discord.getThreadChannelById(
				discordChannelId
			) ?: return

			val guildData = dataService.loadGuildData(discordChannel.guild)

			val messageWrapper = telegramService.receive(update)
			telegramService.send(messageWrapper, discordChannelId, guildData)
		}
	}
}