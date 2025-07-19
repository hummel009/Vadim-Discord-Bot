package com.github.hummel.tdb.core.factory

import com.github.hummel.tdb.core.dao.FileDao
import com.github.hummel.tdb.core.dao.JsonDao
import com.github.hummel.tdb.core.dao.ZipDao
import com.github.hummel.tdb.core.dao.impl.FileDaoImpl
import com.github.hummel.tdb.core.dao.impl.JsonDaoImpl
import com.github.hummel.tdb.core.dao.impl.ZipDaoImpl

@Suppress("unused", "RedundantSuppression")
object DaoFactory {
	val zipDao: ZipDao by lazy { ZipDaoImpl() }
	val jsonDao: JsonDao by lazy { JsonDaoImpl() }
	val fileDao: FileDao by lazy { FileDaoImpl() }
}