package io.github.hummel009.discord.vadim.service.impl

import io.github.hummel009.discord.vadim.bean.Connection
import io.github.hummel009.discord.vadim.factory.ServiceFactory
import io.github.hummel009.discord.vadim.service.AccessService
import io.github.hummel009.discord.vadim.service.DataService
import io.github.hummel009.discord.vadim.service.ManagerService
import io.github.hummel009.discord.vadim.utils.I18n
import io.github.hummel009.discord.vadim.utils.Lang
import io.github.hummel009.discord.vadim.utils.getMessageChannelById
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ManagerServiceImpl : ManagerService {
	private val dataService: DataService = ServiceFactory.dataService
	private val accessService: AccessService = ServiceFactory.accessService

	override fun setLanguage(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "set_language") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)

			accessService.managerAccessRestricted(event, guildData)?.let {
				return@queue
			}

			val embed = run(fun(): MessageEmbed {
				val arguments = event.getOption("arguments")?.asString?.split(" ") ?: emptyList()
				if (arguments.size != 1) {
					return I18n.of("msg_error_arg", guildData).asError(event.member)
				}

				try {
					val lang = requireNotNull(Lang.of(arguments[0]))
					guildData.lang = lang

					val langName = I18n.of(lang.code, guildData)

					return I18n.of("set_language", guildData, langName).asSuccess(event.member)
				} catch (_: Exception) {
					return I18n.of("msg_error_format", guildData).asError(event.member)
				}
			})

			dataService.saveGuildData(guild, guildData)

			event.hook.sendMessageEmbeds(embed).queue()
		}
	}

	override fun addManagerRole(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "add_manager_role") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)

			accessService.managerAccessRestricted(event, guildData)?.let {
				return@queue
			}

			val embed = run(fun(): MessageEmbed {
				val arguments = event.getOption("arguments")?.asString?.split(" ") ?: emptyList()
				if (arguments.size != 1) {
					return I18n.of("msg_error_arg", guildData).asError(event.member)
				}

				try {
					val roleId = arguments[0].toLong().also {
						requireNotNull(guild.getRoleById(it))
					}

					guildData.managerRoleIds.add(roleId)

					return I18n.of("add_manager_role", guildData, roleId).asSuccess(event.member)
				} catch (_: Exception) {
					return I18n.of("msg_error_format", guildData).asError(event.member)
				}
			})

			dataService.saveGuildData(guild, guildData)

			event.hook.sendMessageEmbeds(embed).queue()
		}
	}

	override fun clearManagerRoles(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "clear_manager_roles") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)

			accessService.managerAccessRestricted(event, guildData)?.let {
				return@queue
			}

			val embed = run(fun(): MessageEmbed {
				val arguments = event.getOption("arguments")?.asString?.split(" ") ?: emptyList()
				if (arguments.size !in 0..1) {
					return I18n.of("msg_error_arg", guildData).asError(event.member)
				}

				if (arguments.isEmpty()) {
					guildData.managerRoleIds.clear()

					return I18n.of("clear_manager_roles", guildData).asSuccess(event.member)
				}

				try {
					val roleId = arguments[0].toLong().also {
						requireNotNull(guild.getRoleById(it))
					}

					require(guildData.managerRoleIds.removeIf { it == roleId })

					return I18n.of("clear_manager_roles_single", guildData, roleId).asSuccess(event.member)
				} catch (_: Exception) {
					return I18n.of("msg_error_format", guildData).asError(event.member)
				}
			})

			dataService.saveGuildData(guild, guildData)

			event.hook.sendMessageEmbeds(embed).queue()
		}
	}

	override fun addConnection(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "add_connection") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)

			accessService.managerAccessRestricted(event, guildData)?.let {
				return@queue
			}

			val embed = run(fun(): MessageEmbed {
				val arguments = event.getOption("arguments")?.asString?.split(" ") ?: emptyList()
				if (arguments.size != 2) {
					return I18n.of("msg_error_arg", guildData).asError(event.member)
				}

				try {
					val discordChannelId = arguments[0].toLong().also {
						requireNotNull(guild.getMessageChannelById(it))
					}
					val telegramChatId = arguments[1].toLong()

					require(guildData.localBus.none {
						it.discordChannelId == discordChannelId
					} && guildData.localBus.none {
						it.telegramChatId == telegramChatId
					})

					guildData.localBus.add(Connection(guild.idLong, discordChannelId, telegramChatId))

					return I18n.of(
						"add_connection", guildData, discordChannelId, telegramChatId
					).asSuccess(event.member)
				} catch (_: Exception) {
					return I18n.of("msg_error_format", guildData).asError(event.member)
				}
			})

			dataService.saveGuildData(guild, guildData)

			event.hook.sendMessageEmbeds(embed).queue()
		}
	}

	override fun clearConnections(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "clear_connections") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)

			accessService.managerAccessRestricted(event, guildData)?.let {
				return@queue
			}

			val embed = run(fun(): MessageEmbed {
				val arguments = event.getOption("arguments")?.asString?.split(" ") ?: emptyList()
				if (arguments.size !in 0..1) {
					return I18n.of("msg_error_arg", guildData).asError(event.member)
				}

				if (arguments.isEmpty()) {
					guildData.localBus.clear()

					return I18n.of("clear_connections", guildData).asSuccess(event.member)
				}

				try {
					val discordChannelId = arguments[0].toLong().also {
						requireNotNull(guild.getMessageChannelById(it))
					}
					val telegramChatId = arguments[1].toLong()

					require(guildData.localBus.removeIf {
						it.discordChannelId == discordChannelId && it.telegramChatId == telegramChatId
					})

					return I18n.of("clear_connections_single", guildData, discordChannelId, telegramChatId)
						.asSuccess(event.member)
				} catch (_: Exception) {
					return I18n.of("msg_error_format", guildData).asError(event.member)
				}
			})

			dataService.saveGuildData(guild, guildData)

			event.hook.sendMessageEmbeds(embed).queue()
		}
	}

	override fun commit(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "commit") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)
			val globalData = dataService.loadGlobalData()

			accessService.managerAccessRestricted(event, guildData)?.let {
				return@queue
			}

			val embed = run(fun(): MessageEmbed {
				try {
					require(guildData.localBus.isNotEmpty())

					require(globalData.globalBus.none { gb ->
						val discordClash = guildData.localBus.any { it.discordChannelId == gb.discordChannelId }
						val telegramClash = guildData.localBus.any { it.telegramChatId == gb.telegramChatId }
						discordClash || telegramClash
					})

					globalData.globalBus.addAll(guildData.localBus)

					return I18n.of("commit", guildData).asSuccess(event.member)
				} catch (_: Exception) {
					return I18n.of("msg_error_format", guildData).asError(event.member)
				}
			})

			dataService.saveGlobalData(globalData)

			event.hook.sendMessageEmbeds(embed).queue()
		}
	}

	override fun uncommit(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "uncommit") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)
			val globalData = dataService.loadGlobalData()

			accessService.managerAccessRestricted(event, guildData)?.let {
				return@queue
			}

			val embed = run(fun(): MessageEmbed {
				try {
					require(globalData.globalBus.removeIf {
						it.guildId == guild.idLong
					})

					return I18n.of("uncommit", guildData).asSuccess(event.member)
				} catch (_: Exception) {
					return I18n.of("msg_error_format", guildData).asError(event.member)
				}
			})

			dataService.saveGlobalData(globalData)

			event.hook.sendMessageEmbeds(embed).queue()
		}
	}

	override fun wipeData(event: SlashCommandInteractionEvent) {
		if (event.fullCommandName != "wipe_data") {
			return
		}

		event.deferReply().queue {
			val guild = event.guild ?: return@queue
			val guildData = dataService.loadGuildData(guild)

			accessService.managerAccessRestricted(event, guildData)?.let {
				return@queue
			}

			val embed = run {
				dataService.wipeGuildData(guild)

				I18n.of("wipe_data", guildData).asSuccess(event.member)
			}

			event.hook.sendMessageEmbeds(embed).queue()
		}
	}
}