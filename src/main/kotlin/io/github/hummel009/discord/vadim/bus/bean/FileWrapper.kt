package io.github.hummel009.discord.vadim.bus.bean

import io.github.hummel009.discord.vadim.bus.utils.FileType
import io.github.hummel009.discord.vadim.dao.FileDao
import io.github.hummel009.discord.vadim.factory.DaoFactory
import java.io.InputStream

data class FileWrapper(
	private val fileStream: InputStream,
	private val fileExtension: String?,
	val fileType: FileType,
	val isSpoiler: Boolean?,
) {
	private val fileDao: FileDao = DaoFactory.fileDao

	fun allocateWithPath(): String {
		val tempFolderPath = "temp"
		val tempFilePath = "temp/${System.currentTimeMillis()}.${fileExtension ?: "tmp"}"

		fileDao.createEmptyFolder(tempFolderPath)
		fileDao.createEmptyFile(tempFilePath)

		return runCatching {
			fileDao.getFile(tempFilePath).outputStream().buffered().use { output ->
				fileStream.buffered().use { input ->
					input.copyTo(output, 8192)
				}
			}
			tempFilePath
		}.onFailure { e ->
			e.printStackTrace()

			runCatching {
				fileDao.removeFile(tempFilePath)
			}
		}.getOrElse {
			throw RuntimeException("Failed to allocate file", it)
		}
	}

	fun freeWithPath(tempFilePath: String) {
		runCatching {
			fileDao.removeFile(tempFilePath)
		}.onFailure { e ->
			e.printStackTrace()

			runCatching {
				fileDao.removeFile(tempFilePath)
			}
		}.getOrElse {
			throw RuntimeException("Failed to allocate file", it)
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as FileWrapper

		if (isSpoiler != other.isSpoiler) return false
		if (fileStream != other.fileStream) return false
		if (fileExtension != other.fileExtension) return false
		if (fileType != other.fileType) return false
		if (fileDao != other.fileDao) return false

		return true
	}

	override fun hashCode(): Int {
		var result = isSpoiler?.hashCode() ?: 0
		result = 31 * result + fileStream.hashCode()
		result = 31 * result + (fileExtension?.hashCode() ?: 0)
		result = 31 * result + fileType.hashCode()
		result = 31 * result + fileDao.hashCode()
		return result
	}
}