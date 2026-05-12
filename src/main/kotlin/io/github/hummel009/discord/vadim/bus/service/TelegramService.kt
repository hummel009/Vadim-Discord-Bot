package io.github.hummel009.discord.vadim.bus.service

import io.github.hummel009.discord.vadim.bean.GuildData
import io.github.hummel009.discord.vadim.bus.bean.MessageWrapper
import org.telegram.telegrambots.meta.api.objects.Update

interface TelegramService {
	fun receive(update: Update): MessageWrapper
	fun send(m: MessageWrapper, discordChannelId: Long, guildData: GuildData)
}