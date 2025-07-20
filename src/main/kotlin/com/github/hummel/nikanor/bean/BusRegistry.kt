package com.github.hummel.nikanor.bean

class BusRegistry(
	val discordBus: MutableMap<Long, Long>,
	val telegramBus: MutableMap<Long, Long>
)