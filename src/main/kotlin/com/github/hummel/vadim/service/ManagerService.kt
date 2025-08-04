package com.github.hummel.vadim.service

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

interface ManagerService {
	fun setLanguage(event: SlashCommandInteractionEvent)
	fun addManagerRole(event: SlashCommandInteractionEvent)
	fun clearManagerRoles(event: SlashCommandInteractionEvent)
	fun addConnection(event: SlashCommandInteractionEvent)
	fun clearConnections(event: SlashCommandInteractionEvent)
	fun commit(event: SlashCommandInteractionEvent)
	fun uncommit(event: SlashCommandInteractionEvent)
	fun wipeData(event: SlashCommandInteractionEvent)
}