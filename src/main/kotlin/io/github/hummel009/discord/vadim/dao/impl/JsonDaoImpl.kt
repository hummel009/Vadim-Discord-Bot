package io.github.hummel009.discord.vadim.dao.impl

import io.github.hummel009.discord.vadim.dao.FileDao
import io.github.hummel009.discord.vadim.dao.JsonDao
import io.github.hummel009.discord.vadim.factory.DaoFactory
import io.github.hummel009.discord.vadim.utils.gson

class JsonDaoImpl : io.github.hummel009.discord.vadim.dao.JsonDao {
	private val fileDao: io.github.hummel009.discord.vadim.dao.FileDao = _root_ide_package_.io.github.hummel009.discord.vadim.factory.DaoFactory.fileDao

	override fun <T> readFromFile(filePath: String, clazz: Class<T>): T? {
		val file = fileDao.getFile(filePath)

		if (!file.exists()) {
			return null
		}

		try {
			val json = String(fileDao.readFromFile(filePath))
			return gson.fromJson(json, clazz)
		} catch (_: Exception) {
			return null
		}
	}

	override fun <T> writeToFile(filePath: String, obj: T) {
		val file = fileDao.getFile(filePath)

		if (!file.exists()) {
			fileDao.createEmptyFile(filePath)
		}

		val json = gson.toJson(obj)
		fileDao.writeToFile(filePath, json.toByteArray())
	}
}