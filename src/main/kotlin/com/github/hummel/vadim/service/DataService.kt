package com.github.hummel.vadim.service

import com.github.hummel.vadim.bean.GlobalData
import com.github.hummel.vadim.bean.GuildData
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