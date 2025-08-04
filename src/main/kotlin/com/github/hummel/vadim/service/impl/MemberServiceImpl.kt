package com.github.hummel.vadim.service.impl

import com.github.hummel.vadim.factory.ServiceFactory
import com.github.hummel.vadim.service.DataService
import com.github.hummel.vadim.service.MemberService
import com.github.hummel.vadim.utils.I18n
import com.github.hummel.vadim.utils.success
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

			guildData.managerRoleIds.removeIf { roleId ->
				guild.getRoleById(roleId) == null
			}

			guildData.localBus.removeIf { lc ->
				(guild.getTextChannelById(
					lc.discordChannelId
				) ?: guild.getThreadChannelById(
					lc.discordChannelId
				)) == null
			}

			val text = buildString {
				val langName = I18n.of(guildData.lang, guildData)
				append(I18n.of("info_language", guildData).format(langName), "\r\n")

				if (guildData.managerRoleIds.isEmpty()) {
					append("\r\n", I18n.of("no_manager_roles", guildData), "\r\n")
				} else {
					append("\r\n", I18n.of("has_manager_roles", guildData), "\r\n")
					guildData.managerRoleIds.joinTo(this, "\r\n") { roleId ->
						I18n.of("manager_role", guildData).format(roleId)
					}
					append("\r\n")
				}

				if (guildData.localBus.isEmpty()) {
					append("\r\n", I18n.of("no_connections", guildData), "\r\n")
				} else {
					append("\r\n", I18n.of("has_connections", guildData), "\r\n")
					guildData.localBus.joinTo(this, "\r\n") {
						I18n.of("connection", guildData).format(it.discordChannelId, it.telegramChatId)
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