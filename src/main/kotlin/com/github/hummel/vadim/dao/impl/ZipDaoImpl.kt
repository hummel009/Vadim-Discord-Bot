package com.github.hummel.vadim.dao.impl

import com.github.hummel.vadim.dao.FileDao
import com.github.hummel.vadim.dao.ZipDao
import com.github.hummel.vadim.factory.DaoFactory
import net.lingala.zip4j.ZipFile
import java.io.File

class ZipDaoImpl : ZipDao {
	private val fileDao: FileDao = DaoFactory.fileDao

	override fun unzipFileToFolder(filePath: String, folderPath: String) {
		val file = fileDao.getFile(filePath)
		val folder = fileDao.getFolder(folderPath)

		ZipFile(file.path).extractAll(folder.path)
	}

	override fun zipFolderToFile(folderPath: String, filePath: String) {
		val folder = fileDao.getFolder(folderPath)
		val file = fileDao.getFile(filePath)

		ZipFile(file.path).compressAll(folder)
	}

	private fun ZipFile.compressAll(folder: File) {
		folder.listFiles()?.forEach {
			if (it.isDirectory) {
				addFolder(it)
			} else {
				addFile(it)
			}
		}
	}
}