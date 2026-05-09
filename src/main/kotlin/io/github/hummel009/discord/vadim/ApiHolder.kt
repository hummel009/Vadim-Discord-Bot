package io.github.hummel009.discord.vadim

import io.github.hummel009.discord.vadim.factory.ServiceFactory
import io.github.hummel009.discord.vadim.handler.BusHandler
import io.github.hummel009.discord.vadim.handler.EventHandler
import io.github.hummel009.discord.vadim.utils.config
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import org.telegram.telegrambots.meta.generics.TelegramClient

object ApiHolder {
	val discord: JDA by lazy {
		JDABuilder.createDefault(config.discordToken).apply {
			enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
			enableCache(CacheFlag.entries)
			setMemberCachePolicy(MemberCachePolicy.ALL)
			addEventListeners(EventHandler, BusHandler)
		}.build().awaitReady()
	}

	val telegram: TelegramClient by lazy {
		OkHttpTelegramClient(config.telegramToken)
	}

	fun establishDiscordConnection() {
		discord

		val loginService = ServiceFactory.startService
		loginService.recreateCommands()
	}

	fun establishTelegramConnection() {
		telegram

		TelegramBotsLongPollingApplication().use {
			it.registerBot(config.telegramToken, BusHandler)
			Thread.currentThread().join()
		}
	}
}