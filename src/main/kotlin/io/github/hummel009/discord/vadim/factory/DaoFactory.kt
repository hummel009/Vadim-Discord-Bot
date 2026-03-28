package io.github.hummel009.discord.vadim.factory

import io.github.hummel009.discord.vadim.dao.FileDao
import io.github.hummel009.discord.vadim.dao.JsonDao
import io.github.hummel009.discord.vadim.dao.ZipDao
import io.github.hummel009.discord.vadim.dao.impl.FileDaoImpl
import io.github.hummel009.discord.vadim.dao.impl.JsonDaoImpl
import io.github.hummel009.discord.vadim.dao.impl.ZipDaoImpl

@Suppress("unused", "RedundantSuppression")
object DaoFactory {
	val zipDao: ZipDao by lazy { ZipDaoImpl() }
	val jsonDao: JsonDao by lazy { JsonDaoImpl() }
	val fileDao: FileDao by lazy { FileDaoImpl() }
}