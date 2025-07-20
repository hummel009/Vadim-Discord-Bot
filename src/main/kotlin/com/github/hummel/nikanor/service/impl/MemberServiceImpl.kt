package com.github.hummel.nikanor.service.impl

import com.github.hummel.nikanor.factory.ServiceFactory
import com.github.hummel.nikanor.service.DataService
import com.github.hummel.nikanor.service.MemberService
import com.github.hummel.nikanor.utils.I18n
import com.github.hummel.nikanor.utils.success
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class MemberServiceImpl : MemberService {
	private val dataService: DataService = ServiceFactory.dataService

	override fun info(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "info") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)

			guildData.managerRoleIds.removeIf { guild.getRoleById(it) == null }

			val text = buildString {
				val langName = I18n.of(guildData.lang, guildData)
				append(I18n.of("info_language", guildData).format(langName), "\r\n")
				append(I18n.of("info_discord_channel", guildData).format(guildData.discordChannelId), "\r\n")
				append(I18n.of("info_telegram_chat", guildData).format(guildData.telegramChatId), "\r\n")

				if (guildData.managerRoleIds.isEmpty()) {
					append("\r\n", I18n.of("no_manager_roles", guildData), "\r\n")
				} else {
					append("\r\n", I18n.of("has_manager_roles", guildData), "\r\n")
					guildData.managerRoleIds.joinTo(this, "\r\n") {
						I18n.of("manager_role", guildData).format(it)
					}
					append("\r\n")
				}
			}
			dataService.saveGuildData(guild, guildData)

			val embed = EmbedBuilder().success(event.member, guildData, text)

			event.hook.sendMessageEmbeds(embed).queue()
		}
	}
}