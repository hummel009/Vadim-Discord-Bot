package io.github.hummel009.discord.vadim.bus.service

import io.github.hummel009.discord.vadim.bean.GuildData
import io.github.hummel009.discord.vadim.bus.bean.MessageWrapper
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface DiscordService {
	fun receive(event: MessageReceivedEvent): MessageWrapper
	fun send(m: MessageWrapper, selfId: Long, otherId: Long, guildData: GuildData)
}