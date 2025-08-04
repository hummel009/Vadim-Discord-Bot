package com.github.hummel.vadim.service.impl

import com.github.hummel.vadim.bean.Connection
import com.github.hummel.vadim.factory.ServiceFactory
import com.github.hummel.vadim.service.AccessService
import com.github.hummel.vadim.service.DataService
import com.github.hummel.vadim.service.ManagerService
import com.github.hummel.vadim.utils.I18n
import com.github.hummel.vadim.utils.access
import com.github.hummel.vadim.utils.error
import com.github.hummel.vadim.utils.success
import net.dv8tion.jda.api.EmbedBuilder
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

			val embed = if (!accessService.fromManagerAtLeast(event, guildData)) {
				EmbedBuilder().access(event.member, guildData, I18n.of("msg_access", guildData))
			} else {
				val arguments = event.getOption("arguments")?.asString?.split(" ") ?: emptyList()
				if (arguments.size == 1) {
					try {
						val lang = arguments[0]

						if (lang != "ru" && lang != "be" && lang != "uk" && lang != "en") {
							throw Exception()
						}

						guildData.lang = lang

						val langName = I18n.of(lang, guildData)

						EmbedBuilder().success(
							event.member, guildData, I18n.of("set_language", guildData).format(langName)
						)
					} catch (_: Exception) {
						EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_format", guildData))
					}
				} else {
					EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_arg", guildData))
				}
			}
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

			val embed = if (!accessService.fromManagerAtLeast(event, guildData)) {
				EmbedBuilder().access(event.member, guildData, I18n.of("msg_access", guildData))
			} else {
				val arguments = event.getOption("arguments")?.asString?.split(" ") ?: emptyList()
				if (arguments.size == 1) {
					try {
						val roleId = arguments[0].toLong()

						guild.getRoleById(roleId) ?: throw Exception()

						guildData.managerRoleIds.add(roleId)

						EmbedBuilder().success(
							event.member, guildData, I18n.of("add_manager_role", guildData).format(roleId)
						)
					} catch (_: Exception) {
						EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_format", guildData))
					}
				} else {
					EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_arg", guildData))
				}
			}
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

			val embed = if (!accessService.fromManagerAtLeast(event, guildData)) {
				EmbedBuilder().access(event.member, guildData, I18n.of("msg_access", guildData))
			} else {
				val arguments = event.getOption("arguments")?.asString?.split(" ") ?: emptyList()
				if (arguments.isEmpty()) {
					guildData.managerRoleIds.clear()

					EmbedBuilder().success(event.member, guildData, I18n.of("clear_manager_roles", guildData))
				} else {
					if (arguments.size == 1) {
						try {
							val roleId = arguments[0].toLong()

							if (!guildData.managerRoleIds.removeIf { it == roleId }) {
								throw Exception()
							}

							EmbedBuilder().success(
								event.member, guildData, I18n.of("clear_manager_roles_single", guildData).format(roleId)
							)
						} catch (_: Exception) {
							EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_format", guildData))
						}
					} else {
						EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_arg", guildData))
					}
				}
			}
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

			val embed = if (!accessService.fromManagerAtLeast(event, guildData)) {
				EmbedBuilder().access(event.member, guildData, I18n.of("msg_access", guildData))
			} else {
				val arguments = event.getOption("arguments")?.asString?.split(" ") ?: emptyList()
				if (arguments.size == 2) {
					try {
						val discordChannelId = arguments[0].toLong()
						val telegramChatId = arguments[1].toLong()

						guild.getTextChannelById(
							discordChannelId
						) ?: guild.getThreadChannelById(
							discordChannelId
						) ?: throw Exception()

						val noClash = guildData.localBus.none {
							it.discordChannelId == discordChannelId
						} && guildData.localBus.none {
							it.telegramChatId == telegramChatId
						}

						if (noClash) {
							guildData.localBus.add(Connection(guildData.guildId, discordChannelId, telegramChatId))
						} else {
							throw Exception()
						}

						EmbedBuilder().success(
							event.member,
							guildData,
							I18n.of("add_connection", guildData).format(discordChannelId, telegramChatId)
						)
					} catch (_: Exception) {
						EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_format", guildData))
					}
				} else {
					EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_arg", guildData))
				}
			}
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

			val embed = if (!accessService.fromManagerAtLeast(event, guildData)) {
				EmbedBuilder().access(event.member, guildData, I18n.of("msg_access", guildData))
			} else {
				val arguments = event.getOption("arguments")?.asString?.split(" ") ?: emptyList()
				if (arguments.isEmpty()) {
					guildData.localBus.clear()

					EmbedBuilder().success(event.member, guildData, I18n.of("clear_connections", guildData))
				} else {
					if (arguments.size == 1) {
						try {
							val discordChannelId = arguments[0].toLong()

							if (!guildData.localBus.removeIf { it.discordChannelId == discordChannelId }) {
								throw Exception()
							}

							EmbedBuilder().success(
								event.member,
								guildData,
								I18n.of("clear_connections_single", guildData).format(discordChannelId)
							)
						} catch (_: Exception) {
							EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_format", guildData))
						}
					} else {
						EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_arg", guildData))
					}
				}
			}
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
			val busRegistry = dataService.loadGlobalData()

			val embed = if (!accessService.fromManagerAtLeast(event, guildData)) {
				EmbedBuilder().access(event.member, guildData, I18n.of("msg_access", guildData))
			} else {
				try {
					if (guildData.localBus.isEmpty()) {
						throw Exception()
					}

					val noClash = busRegistry.globalBus.none { gc ->
						guildData.localBus.any { lc ->
							gc.discordChannelId == lc.discordChannelId
						} || guildData.localBus.any { lc ->
							gc.telegramChatId == lc.telegramChatId
						}
					}

					if (noClash) {
						busRegistry.globalBus.addAll(guildData.localBus)
					} else {
						throw Exception()
					}

					EmbedBuilder().success(
						event.member, guildData, I18n.of("commit", guildData)
					)
				} catch (_: Exception) {
					EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_format", guildData))
				}
			}
			dataService.saveGlobalData(busRegistry)

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
			val busRegistry = dataService.loadGlobalData()

			val embed = if (!accessService.fromManagerAtLeast(event, guildData)) {
				EmbedBuilder().access(event.member, guildData, I18n.of("msg_access", guildData))
			} else {
				try {
					busRegistry.globalBus.removeIf {
						it.guildId == guildData.guildId
					}

					EmbedBuilder().success(
						event.member, guildData, I18n.of("uncommit", guildData)
					)
				} catch (_: Exception) {
					EmbedBuilder().error(event.member, guildData, I18n.of("msg_error_format", guildData))
				}
			}
			dataService.saveGlobalData(busRegistry)

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

			val embed = if (!accessService.fromManagerAtLeast(event, guildData)) {
				EmbedBuilder().access(event.member, guildData, I18n.of("msg_access", guildData))
			} else {
				dataService.wipeGuildData(guild)

				EmbedBuilder().success(event.member, guildData, I18n.of("wipe_data", guildData))
			}
			event.hook.sendMessageEmbeds(embed).queue()
		}
	}
}