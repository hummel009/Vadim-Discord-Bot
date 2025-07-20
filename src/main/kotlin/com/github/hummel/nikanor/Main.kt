package com.github.hummel.nikanor

import com.github.hummel.nikanor.bean.BotData
import com.github.hummel.nikanor.factory.ServiceFactory
import com.github.hummel.nikanor.utils.gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class Config(
	val discordToken: String, val telegramToken: String, val ownerId: String, val reinit: String?
)

fun main() {
	try {
		val file = File("config.json")
		if (file.exists()) {
			FileReader(file).use {
				val config = gson.fromJson(it, Config::class.java)

				launchWithData(config, "files")
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

	val config = Config(discordToken, telegramToken, ownerId, "false")
	try {
		val file = File("config.json")
		FileWriter(file).use {
			gson.toJson(config, it)
		}
	} catch (e: Exception) {
		e.printStackTrace()
	}

	launchWithData(config, "files")
}

@Suppress("UNUSED_PARAMETER")
fun launchWithData(
	config: Config, root: String
) {
	BotData.discordToken = config.discordToken
	BotData.telegramToken = config.telegramToken
	BotData.ownerId = config.ownerId
	BotData.root = root

	val loginService = ServiceFactory.loginService
	loginService.loginBot(config.reinit.toBoolean())
}