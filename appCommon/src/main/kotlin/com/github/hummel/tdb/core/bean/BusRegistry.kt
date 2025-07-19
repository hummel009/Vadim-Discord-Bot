package com.github.hummel.tdb.core.bean

class BusRegistry(
	val discordBus: MutableMap<Long, Long>,
	val telegramBus: MutableMap<Long, Long>
)