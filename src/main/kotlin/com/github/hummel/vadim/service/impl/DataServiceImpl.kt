package com.github.hummel.vadim.service.impl

import com.github.hummel.vadim.bean.GlobalData
import com.github.hummel.vadim.bean.GuildData
import com.github.hummel.vadim.dao.FileDao
import com.github.hummel.vadim.dao.JsonDao
import com.github.hummel.vadim.dao.ZipDao
import com.github.hummel.vadim.factory.DaoFactory
import com.github.hummel.vadim.service.DataService
import net.dv8tion.jda.api.entities.Guild

class DataServiceImpl : DataService {
	private val fileDao: FileDao = DaoFactory.fileDao
	private val jsonDao: JsonDao = DaoFactory.jsonDao
	private val zipDao: ZipDao = DaoFactory.zipDao

	override fun loadGuildData(guild: Guild): GuildData {
		val folderName = guild.id
		val filePath = "guilds/$folderName/data.json"

		return jsonDao.readFromFile(filePath, GuildData::class.java) ?: initAndGetGuildData(guild)
	}

	override fun saveGuildData(guild: Guild, guildData: GuildData) {
		val folderName = guild.id
		val filePath = "guilds/$folderName/data.json"

		jsonDao.writeToFile(filePath, guildData)
	}

	override fun loadGlobalData(): GlobalData {
		val filePath = "guilds/data.json"

		return jsonDao.readFromFile(filePath, GlobalData::class.java) ?: initAndGetGlobalData()
	}

	override fun saveGlobalData(globalData: GlobalData) {
		val filePath = "guilds/data.json"

		fileDao.createEmptyFile(filePath)
		jsonDao.writeToFile(filePath, globalData)
	}

	override fun wipeGuildData(guild: Guild) {
		val folderName = guild.id
		val filePath = "guilds/$folderName/data.json"

		fileDao.removeFile(filePath)
		fileDao.createEmptyFile(filePath)
	}

	override fun importBotData(byteArray: ByteArray) {
		val targetFolderPath = "guilds"
		val importFolderPath = "import"
		val importFilePath = "import/bot.zip"

		fileDao.createEmptyFolder(importFolderPath)
		fileDao.createEmptyFile(importFilePath)
		fileDao.writeToFile(importFilePath, byteArray)

		fileDao.removeFolder(targetFolderPath)
		fileDao.createEmptyFolder(targetFolderPath)

		zipDao.unzipFileToFolder(importFilePath, targetFolderPath)

		fileDao.removeFile(importFilePath)
		fileDao.removeFolder(importFolderPath)
	}

	override fun exportBotData(): ByteArray {
		val targetFolderPath = "guilds"
		val exportFolderPath = "export"
		val exportFilePath = "export/bot.zip"

		fileDao.createEmptyFolder(exportFolderPath)
		zipDao.zipFolderToFile(targetFolderPath, exportFilePath)

		val file = fileDao.readFromFile(exportFilePath)

		fileDao.removeFile(exportFilePath)
		fileDao.removeFolder(exportFolderPath)

		return file
	}

	private fun initAndGetGuildData(guild: Guild): GuildData = GuildData(
		guildName = guild.name,
		lang = "ru",
		managerRoleIds = mutableSetOf(),
		localBus = mutableSetOf()
	)

	private fun initAndGetGlobalData(): GlobalData = GlobalData(mutableSetOf())
}