package com.github.hummel.vadim.bean

data class GuildData(
	val guildId: Long,
	val guildName: String,
	var lang: String,
	val managerRoleIds: MutableSet<Long>,
	var localBus: MutableSet<Connection>
)