package com.github.hummel.vadim.service.impl

import com.github.hummel.vadim.ApiHolder
import com.github.hummel.vadim.bean.BotData
import com.github.hummel.vadim.handler.BusHandler
import com.github.hummel.vadim.handler.EventHandler
import com.github.hummel.vadim.service.LoginService
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import kotlin.concurrent.thread

class LoginServiceImpl : LoginService {
	override fun loginBot(reinit: Boolean) {
		thread {
			ApiHolder.discord = JDABuilder.createDefault(BotData.discordToken).apply {
				enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
				enableCache(CacheFlag.entries)
				setMemberCachePolicy(MemberCachePolicy.ALL)
				addEventListeners(EventHandler, BusHandler)
			}.build().awaitReady()

			if (reinit) {
				recreateCommands()
			}
		}

		thread {
			ApiHolder.telegram = OkHttpTelegramClient(BotData.telegramToken)

			TelegramBotsLongPollingApplication().use {
				it.registerBot(BotData.telegramToken, BusHandler)
				Thread.currentThread().join()
			}
		}
	}

	@Suppress("unused")
	private fun recreateCommands() {
		fun String.cmd(description: String, options: List<OptionData>) =
			Commands.slash(this, description).addOptions(options)

		val commands = listOf(
			"info".cmd("/info", empty()),

			"set_language".cmd("/set_language [ru/be/uk/en]", string()),

			"add_manager_role".cmd("/add_manager_role [role_id]", string()),
			"clear_manager_roles".cmd("/clear_manager_roles {role_id}", string(false)),

			"add_connection".cmd("/add_connection [discord_channel_id] [telegram_chat_id]", string()),
			"clear_connections".cmd("/clear_connections {discord_channel_id}", string(false)),

			"commit".cmd("/commit", empty()),
			"uncommit".cmd("/uncommit", string(false)),

			"wipe_data".cmd("/wipe_data", empty()),

			"import".cmd("/import", attachment()),
			"export".cmd("/export", empty()),
			"exit".cmd("/exit", empty())
		)
		ApiHolder.discord.updateCommands().addCommands(commands).complete()
	}

	private fun empty(): List<OptionData> = emptyList()

	private fun string(obligatory: Boolean = true): List<OptionData> = listOf(
		OptionData(OptionType.STRING, "arguments", "The list of arguments", obligatory)
	)

	private fun attachment(): List<OptionData> = listOf(
		OptionData(OptionType.ATTACHMENT, "arguments", "The list of arguments", true)
	)
}