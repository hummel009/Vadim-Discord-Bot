package io.github.hummel009.discord.vadim.service.impl

import io.github.hummel009.discord.vadim.ApiHolder
import io.github.hummel009.discord.vadim.service.StartService
import io.github.hummel009.discord.vadim.utils.Lang
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
			withoutOptions("commit"),
			withoutOptions("exit"),
			withoutOptions("export"),
			withoutOptions("info"),
			withoutOptions("uncommit"),
			withoutOptions("wipe_data"),

			withStringOption("add_connection", "[discord_channel_id] [telegram_chat_id]"),
			withStringOption("add_manager_role", "[role_id]"),
			withStringOption("set_language", "[${Lang.entries.joinToString("|")}]"),

			withStringOption("clear_connections", "{discord_channel_id} {telegram_chat_id}", false),
			withStringOption("clear_manager_roles", "{role_id}", false),

			withAttachmentOption("import")
		)

		ApiHolder.discord.updateCommands().addCommands(commands).complete()
	}

	private fun withoutOptions(command: String): SlashCommandData =
		Commands.slash(command, "/$command").addOptions(emptyList())

	private fun withStringOption(command: String, parameters: String, obligatory: Boolean = true): SlashCommandData =
		Commands.slash(command, "/$command $parameters")
			.addOptions(OptionData(OptionType.STRING, "arguments", parameters, obligatory))

	private fun withAttachmentOption(command: String): SlashCommandData =
		Commands.slash(command, "/$command")
			.addOptions(OptionData(OptionType.ATTACHMENT, "arguments", "", true))
}