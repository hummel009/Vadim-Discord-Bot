package io.github.hummel009.discord.vadim

import net.dv8tion.jda.api.JDA
import org.telegram.telegrambots.meta.generics.TelegramClient

object ApiHolder {
	lateinit var discord: JDA
	lateinit var telegram: TelegramClient
}