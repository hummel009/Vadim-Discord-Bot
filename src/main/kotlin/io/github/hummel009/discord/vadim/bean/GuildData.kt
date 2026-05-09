package io.github.hummel009.discord.vadim.bean

import io.github.hummel009.discord.vadim.utils.Lang

data class GuildData(
	val guildName: String,
	var lang: Lang,
	val managerRoleIds: MutableSet<Long>,
	var localBus: MutableSet<Connection>
)