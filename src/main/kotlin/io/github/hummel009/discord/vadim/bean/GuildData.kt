package io.github.hummel009.discord.vadim.bean

data class GuildData(
	val guildName: String,
	var lang: String,
	val managerRoleIds: MutableSet<Long>,
	var localBus: MutableSet<Connection>
)