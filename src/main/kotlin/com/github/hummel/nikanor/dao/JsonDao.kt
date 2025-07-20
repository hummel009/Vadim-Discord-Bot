package com.github.hummel.nikanor.dao

interface JsonDao {
	fun <T> readFromFile(filePath: String, clazz: Class<T>): T?
	fun <T> writeToFile(filePath: String, obj: T)
}