package io.github.hummel009.discord.vadim.bus.bean

import io.github.hummel009.discord.vadim.bus.utils.FileType
import io.github.hummel009.discord.vadim.dao.FileDao
import io.github.hummel009.discord.vadim.factory.DaoFactory

data class FileWrapper(
	private val fileBytes: ByteArray,
	private val fileExtension: String?,
	val fileType: FileType,
	val isSpoiler: Boolean?,
) {
	private val fileDao: FileDao = DaoFactory.fileDao

	override fun equals(other: Any?): Boolean = false

	override fun hashCode(): Int = 0

	fun allocateWithPath(): String {
		val tempFolderPath = "temp"
		val tempFilePath = "temp/${System.currentTimeMillis()}.$fileExtension"

		fileDao.createEmptyFolder(tempFolderPath)
		fileDao.createEmptyFile(tempFilePath)
		fileDao.writeToFile(tempFilePath, fileBytes)

		return tempFilePath
	}

	fun freeWithPath(tempFilePath: String) {
		fileDao.removeFile(tempFilePath)
	}
}