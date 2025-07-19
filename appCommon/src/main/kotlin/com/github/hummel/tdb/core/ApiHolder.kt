package com.github.hummel.tdb.core

import net.dv8tion.jda.api.JDA
import org.telegram.telegrambots.meta.generics.TelegramClient

object ApiHolder {
	lateinit var discord: JDA
	lateinit var telegram: TelegramClient
}