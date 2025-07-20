package com.github.hummel.nikanor.dao

interface ZipDao {
	fun unzipFileToFolder(filePath: String, folderPath: String)
	fun zipFolderToFile(folderPath: String, filePath: String)
}