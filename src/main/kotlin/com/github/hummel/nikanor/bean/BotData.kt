package com.github.hummel.nikanor.bean

object BotData {
	lateinit var root: String
	lateinit var discordToken: String
	lateinit var telegramToken: String
	lateinit var ownerId: String
	lateinit var exitFunction: () -> Unit
}