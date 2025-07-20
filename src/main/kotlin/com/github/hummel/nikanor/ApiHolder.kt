package com.github.hummel.nikanor

import net.dv8tion.jda.api.JDA
import org.telegram.telegrambots.meta.generics.TelegramClient

object ApiHolder {
	lateinit var discord: JDA
	lateinit var telegram: TelegramClient
}