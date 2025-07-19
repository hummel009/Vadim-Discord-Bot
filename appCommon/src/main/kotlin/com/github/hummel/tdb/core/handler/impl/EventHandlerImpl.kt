package com.github.hummel.tdb.core.handler.impl

import com.github.hummel.tdb.core.factory.ServiceFactory
import com.github.hummel.tdb.core.service.ManagerService
import com.github.hummel.tdb.core.service.MemberService
import com.github.hummel.tdb.core.service.OwnerService
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class EventHandlerImpl : ListenerAdapter() {
	private val memberService: MemberService = ServiceFactory.memberService
	private val managerService: ManagerService = ServiceFactory.managerService
	private val ownerService: OwnerService = ServiceFactory.ownerService

	override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
		memberService.info(event)

		managerService.addManagerRole(event)
		managerService.clearManagerRoles(event)

		managerService.setLanguage(event)

		managerService.setDiscordChannel(event)
		managerService.setTelegramChat(event)
		managerService.commit(event)

		managerService.wipeData(event)

		ownerService.import(event)
		ownerService.export(event)
		ownerService.exit(event)
	}
}