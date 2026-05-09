package io.github.hummel009.discord.vadim

import io.github.hummel009.discord.vadim.utils.gson
import io.github.hummel009.discord.vadim.utils.input
import java.io.File
import java.io.FileWriter
import kotlin.concurrent.thread

data class Config(
	val discordToken: String, val telegramToken: String, val ownerId: String, val reinit: Boolean
)

fun main() {
	ensureConfigExists()

	thread { ApiHolder.establishDiscordConnection() }
	thread { ApiHolder.establishTelegramConnection() }
}

fun ensureConfigExists() {
	val file = File(input, "config.json")
	if (file.exists()) {
		return
	}

	print("Enter the Discord Token: ")
	val discordToken = readln()

	print("Enter the Telegram Token: ")
	val telegramToken = readln()

	print("Enter the Owner ID: ")
	val ownerId = readln()

	print("Reinit? Type true/false: ")
	val reinit = readln()

	FileWriter(file).use {
		gson.toJson(Config(discordToken, telegramToken, ownerId, reinit.toBoolean()), it)
	}
}