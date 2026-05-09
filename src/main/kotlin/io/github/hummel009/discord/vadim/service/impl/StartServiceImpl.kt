package io.github.hummel009.discord.vadim.service.impl

import io.github.hummel009.discord.vadim.ApiHolder
import io.github.hummel009.discord.vadim.service.StartService
import io.github.hummel009.discord.vadim.utils.config
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class StartServiceImpl : StartService {
	override fun recreateCommands() {
		if (!config.reinit) {
			return
		}

		val commands = listOf(
			"info".cmd("/info", empty()),

			"set_language".cmd("/set_language [ru/be/uk/en]", string()),

			"add_manager_role".cmd("/add_manager_role [role_id]", string()),
			"clear_manager_roles".cmd("/clear_manager_roles {role_id}", string(false)),

			"add_connection".cmd("/add_connection [discord_channel_id] [telegram_chat_id]", string()),
			"clear_connections".cmd("/clear_connections {discord_channel_id} {telegram_chat_id}", string(false)),

			"commit".cmd("/commit", empty()),
			"uncommit".cmd("/uncommit", string(false)),

			"wipe_data".cmd("/wipe_data", empty()),

			"import".cmd("/import", attachment()),
			"export".cmd("/export", empty()),
			"exit".cmd("/exit", empty())
		)

		ApiHolder.discord.updateCommands().addCommands(commands).complete()
	}

	private fun String.cmd(description: String, options: List<OptionData>): SlashCommandData =
		Commands.slash(this, description).addOptions(options)

	private fun empty(): List<OptionData> = emptyList()

	private fun string(obligatory: Boolean = true): List<OptionData> = listOf(
		OptionData(OptionType.STRING, "arguments", "The list of arguments", obligatory)
	)

	private fun attachment(): List<OptionData> = listOf(
		OptionData(OptionType.ATTACHMENT, "arguments", "The list of arguments", true)
	)
}