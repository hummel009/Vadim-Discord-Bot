package io.github.hummel009.discord.vadim.service

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

interface MemberService {
	fun info(event: SlashCommandInteractionEvent)
}