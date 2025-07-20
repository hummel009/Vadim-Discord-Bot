package com.github.hummel.nikanor.factory

import com.github.hummel.nikanor.dao.FileDao
import com.github.hummel.nikanor.dao.JsonDao
import com.github.hummel.nikanor.dao.ZipDao
import com.github.hummel.nikanor.dao.impl.FileDaoImpl
import com.github.hummel.nikanor.dao.impl.JsonDaoImpl
import com.github.hummel.nikanor.dao.impl.ZipDaoImpl

@Suppress("unused", "RedundantSuppression")
object DaoFactory {
	val zipDao: ZipDao by lazy { ZipDaoImpl() }
	val jsonDao: JsonDao by lazy { JsonDaoImpl() }
	val fileDao: FileDao by lazy { FileDaoImpl() }
}