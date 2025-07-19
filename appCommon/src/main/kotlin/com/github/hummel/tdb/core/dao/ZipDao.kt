package com.github.hummel.tdb.core.dao

interface ZipDao {
	fun unzipFileToFolder(filePath: String, folderPath: String)
	fun zipFolderToFile(folderPath: String, filePath: String)
}