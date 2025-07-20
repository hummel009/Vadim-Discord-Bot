package com.github.hummel.nikanor.bean

data class GuildData(
	val guildId: String,
	val guildName: String,
	var lang: String,
	var discordChannelId: Long,
	var telegramChatId: Long,
	val managerRoleIds: MutableSet<Long>
)