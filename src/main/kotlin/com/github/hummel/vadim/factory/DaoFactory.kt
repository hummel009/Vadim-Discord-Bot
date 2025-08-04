package com.github.hummel.vadim.factory

import com.github.hummel.vadim.dao.FileDao
import com.github.hummel.vadim.dao.JsonDao
import com.github.hummel.vadim.dao.ZipDao
import com.github.hummel.vadim.dao.impl.FileDaoImpl
import com.github.hummel.vadim.dao.impl.JsonDaoImpl
import com.github.hummel.vadim.dao.impl.ZipDaoImpl

@Suppress("unused", "RedundantSuppression")
object DaoFactory {
	val zipDao: ZipDao by lazy { ZipDaoImpl() }
	val jsonDao: JsonDao by lazy { JsonDaoImpl() }
	val fileDao: FileDao by lazy { FileDaoImpl() }
}