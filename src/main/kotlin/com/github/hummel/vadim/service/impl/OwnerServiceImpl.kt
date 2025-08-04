package com.github.hummel.vadim.service.impl

import com.github.hummel.vadim.factory.ServiceFactory
import com.github.hummel.vadim.service.AccessService
import com.github.hummel.vadim.service.DataService
import com.github.hummel.vadim.service.OwnerService
import com.github.hummel.vadim.utils.I18n
import com.github.hummel.vadim.utils.access
import com.github.hummel.vadim.utils.error
import com.github.hummel.vadim.utils.success
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.FileProxy
import net.dv8tion.jda.api.utils.FileUpload
import kotlin.system.exitProcess

class OwnerServiceImpl : OwnerService {
	private val dataService: DataService = ServiceFactory.dataService
	private val accessService: AccessService = ServiceFactory.accessService

	override fun import(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "import") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)

			if (!accessService.fromOwnerAtLeast(event)) {
				val embed = EmbedBuilder().access(event.member, guildData, I18n.of("msg_access", guildData))

				event.hook.sendMessageEmbeds(embed).queue()
			} else {
				try {
					val attachment = event.getOption("arguments")?.asAttachment ?: throw Exception()
					val byteArray = FileProxy(attachment.url).download().join().readBytes()

					dataService.importBotData(byteArray)

					val embed = EmbedBuilder().success(event.member, guildData, I18n.of("import", guildData))

					event.hook.sendMessageEmbeds(embed).queue()
				} catch (_: Exception) {
					val embed = EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_format", guildData))

					event.hook.sendMessageEmbeds(embed).queue()
				}
			}
		}
	}

	override fun export(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "export") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)

			if (!accessService.fromOwnerAtLeast(event)) {
				val embed = EmbedBuilder().access(event.member, guildData, I18n.of("msg_access", guildData))

				event.hook.sendMessageEmbeds(embed).queue()
			} else {
				try {
					val byteArray = dataService.exportBotData()

					event.hook.sendFiles(FileUpload.fromData(byteArray, "bot.zip")).queue()
				} catch (_: Exception) {
					val embed = EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_format", guildData))

					event.hook.sendMessageEmbeds(embed).queue()
				}
			}
		}
	}

	override fun exit(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "exit") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)

			if (!accessService.fromOwnerAtLeast(event)) {
				val embed = EmbedBuilder().access(event.member, guildData, I18n.of("msg_access", guildData))

				event.hook.sendMessageEmbeds(embed).queue()
			} else {
				val embed = EmbedBuilder().success(event.member, guildData, I18n.of("exit", guildData))

				event.hook.sendMessageEmbeds(embed).queue {
					exitProcess(0)
				}
			}
		}
	}
}