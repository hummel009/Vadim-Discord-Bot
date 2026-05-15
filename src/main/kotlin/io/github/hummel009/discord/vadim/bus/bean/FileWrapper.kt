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

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as FileWrapper

		if (isSpoiler != other.isSpoiler) return false
		if (!fileBytes.contentEquals(other.fileBytes)) return false
		if (fileExtension != other.fileExtension) return false
		if (fileType != other.fileType) return false
		if (fileDao != other.fileDao) return false

		return true
	}

	override fun hashCode(): Int {
		var result = isSpoiler?.hashCode() ?: 0
		result = 31 * result + fileBytes.contentHashCode()
		result = 31 * result + (fileExtension?.hashCode() ?: 0)
		result = 31 * result + fileType.hashCode()
		result = 31 * result + fileDao.hashCode()
		return result
	}
}