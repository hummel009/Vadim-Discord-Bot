package io.github.hummel009.discord.vadim

import io.github.hummel009.discord.vadim.bean.BotData
import io.github.hummel009.discord.vadim.factory.ServiceFactory
import io.github.hummel009.discord.vadim.utils.gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class Config(
	val discordToken: String, val telegramToken: String, val ownerId: String, val reinit: Boolean
)

fun main() {
	try {
		val file = File("input/config.json")
		if (file.exists()) {
			FileReader(file).use {
				val config = _root_ide_package_.io.github.hummel009.discord.vadim.utils.gson.fromJson(it, _root_ide_package_.io.github.hummel009.discord.vadim.Config::class.java)

				_root_ide_package_.io.github.hummel009.discord.vadim.launchWithData(config, "output")
			}
		} else {
			_root_ide_package_.io.github.hummel009.discord.vadim.requestUserInput()
		}
	} catch (_: Exception) {
		_root_ide_package_.io.github.hummel009.discord.vadim.requestUserInput()
	}
}

fun requestUserInput() {
	print("Enter the Discord Token: ")
	val discordToken = readln()

	print("Enter the Telegram Token: ")
	val telegramToken = readln()

	print("Enter the Owner ID: ")
	val ownerId = readln()

	print("Reinit? Type true/false: ")
	val reinit = readln()

	val config = _root_ide_package_.io.github.hummel009.discord.vadim.Config(
		discordToken,
		telegramToken,
		ownerId,
		reinit.toBoolean()
	)
	try {
		val file = File("input/config.json")
		FileWriter(file).use {
			_root_ide_package_.io.github.hummel009.discord.vadim.utils.gson.toJson(config, it)
		}
	} catch (e: Exception) {
		e.printStackTrace()
	}

	_root_ide_package_.io.github.hummel009.discord.vadim.launchWithData(config, "output")
}

fun launchWithData(config: io.github.hummel009.discord.vadim.Config, root: String) {
	_root_ide_package_.io.github.hummel009.discord.vadim.bean.BotData.discordToken = config.discordToken
	_root_ide_package_.io.github.hummel009.discord.vadim.bean.BotData.telegramToken = config.telegramToken
	_root_ide_package_.io.github.hummel009.discord.vadim.bean.BotData.ownerId = config.ownerId
	_root_ide_package_.io.github.hummel009.discord.vadim.bean.BotData.root = root

	val loginService = _root_ide_package_.io.github.hummel009.discord.vadim.factory.ServiceFactory.loginService
	loginService.loginBot(config.reinit)
}