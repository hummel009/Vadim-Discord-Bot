package com.github.hummel.tdb.windows

import com.github.hummel.tdb.core.bean.BotData
import com.github.hummel.tdb.core.utils.gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.system.exitProcess

data class Config(
	val discordToken: String?, val telegramToken: String?, val ownerId: String?
)

fun main() {
	try {
		val file = File("config.json")
		if (file.exists()) {
			FileReader(file).use { reader ->
				val config = gson.fromJson(reader, Config::class.java)
				if (config.discordToken != null && config.telegramToken != null && config.ownerId != null) {
					launchWithData(config.discordToken, config.telegramToken, config.ownerId, "files")
				} else {
					requestUserInput()
				}
			}
		} else {
			requestUserInput()
		}
	} catch (_: Exception) {
		requestUserInput()
	}
}

fun requestUserInput() {
	print("Enter the Discord Token: ")
	val discordToken = readln()

	print("Enter the Telegram Token: ")
	val telegramToken = readln()

	print("Enter the Owner ID: ")
	val ownerId = readln()

	val config = Config(discordToken, telegramToken, ownerId)
	try {
		val file = File("config.json")
		if (!file.exists()) {
			FileWriter(file).use { writer ->
				gson.toJson(config, writer)
			}
		}
	} catch (e: Exception) {
		e.printStackTrace()
	}

	launchWithData(discordToken, telegramToken, ownerId, "files")
}

@Suppress("UNUSED_PARAMETER")
fun launchWithData(discordToken: String, telegramToken: String, ownerId: String, root: String) {
	BotData.discordToken = discordToken
	BotData.telegramToken = telegramToken
	BotData.ownerId = ownerId
	BotData.root = root
	BotData.exitFunction = { exitFunction() }

	startFunction()
}

fun startFunction() {
	val adapter = DiscordAdapter()
	adapter.launch()
}

fun exitFunction() {
	exitProcess(0)
}