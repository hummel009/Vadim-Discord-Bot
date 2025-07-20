package com.github.hummel.nikanor.service

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

interface ManagerService {
	fun addManagerRole(event: SlashCommandInteractionEvent)
	fun clearManagerRoles(event: SlashCommandInteractionEvent)
	fun setLanguage(event: SlashCommandInteractionEvent)
	fun setDiscordChannel(event: SlashCommandInteractionEvent)
	fun setTelegramChat(event: SlashCommandInteractionEvent)
	fun commit(event: SlashCommandInteractionEvent)
	fun uncommit(event: SlashCommandInteractionEvent)
	fun wipeData(event: SlashCommandInteractionEvent)
}