package com.github.hummel.nikanor.bean

class BusRegistry(
	val discordToTelegram: MutableMap<Long, Long>,
	val telegramToDiscord: MutableMap<Long, Long>,
	val ownership: MutableMap<Long, List<Long>>
)