package io.github.hummel009.discord.vadim.service

import io.github.hummel009.discord.vadim.bean.GlobalData
import io.github.hummel009.discord.vadim.bean.GuildData
import net.dv8tion.jda.api.entities.Guild

interface DataService {
	fun loadGuildData(guild: Guild): GuildData
	fun saveGuildData(guild: Guild, guildData: GuildData)

	fun loadGlobalData(): GlobalData
	fun saveGlobalData(globalData: GlobalData)

	fun wipeGuildData(guild: Guild)

	fun exportBotData(): ByteArray
	fun importBotData(byteArray: ByteArray)
}