package io.github.hummel009.discord.vadim.factory

import io.github.hummel009.discord.vadim.bus.service.DiscordService
import io.github.hummel009.discord.vadim.bus.service.TelegramService
import io.github.hummel009.discord.vadim.bus.service.impl.DiscordServiceImpl
import io.github.hummel009.discord.vadim.bus.service.impl.TelegramServiceImpl
import io.github.hummel009.discord.vadim.service.*
import io.github.hummel009.discord.vadim.service.impl.*

@Suppress("unused", "RedundantSuppression")
object ServiceFactory {
	val startService: StartService by lazy { StartServiceImpl() }

	val memberService: MemberService by lazy { MemberServiceImpl() }
	val managerService: ManagerService by lazy { ManagerServiceImpl() }
	val ownerService: OwnerService by lazy { OwnerServiceImpl() }

	val dataService: DataService by lazy { DataServiceImpl() }
	val accessService: AccessService by lazy { AccessServiceImpl() }

	val discordService: DiscordService by lazy { DiscordServiceImpl() }
	val telegramService: TelegramService by lazy { TelegramServiceImpl() }
}