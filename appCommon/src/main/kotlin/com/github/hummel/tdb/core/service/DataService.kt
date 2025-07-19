package com.github.hummel.tdb.core.service

import com.github.hummel.tdb.core.bean.BusRegistry
import com.github.hummel.tdb.core.bean.GuildData
import net.dv8tion.jda.api.entities.Guild

interface DataService {
	fun loadGuildData(guild: Guild): GuildData
	fun saveGuildData(guild: Guild, guildData: GuildData)

	fun loadBusRegistry(): BusRegistry
	fun saveBusRegistry(busRegistry: BusRegistry)

	fun wipeGuildData(guild: Guild)

	fun exportBotData(): ByteArray
	fun importBotData(byteArray: ByteArray)
}