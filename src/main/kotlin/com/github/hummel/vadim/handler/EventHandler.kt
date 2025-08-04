package com.github.hummel.vadim.handler

import com.github.hummel.vadim.factory.ServiceFactory
import com.github.hummel.vadim.service.ManagerService
import com.github.hummel.vadim.service.MemberService
import com.github.hummel.vadim.service.OwnerService
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object EventHandler : ListenerAdapter() {
	private val memberService: MemberService = ServiceFactory.memberService
	private val managerService: ManagerService = ServiceFactory.managerService
	private val ownerService: OwnerService = ServiceFactory.ownerService

	override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
		memberService.info(event)

		managerService.addManagerRole(event)
		managerService.clearManagerRoles(event)

		managerService.setLanguage(event)

		managerService.addConnection(event)
		managerService.clearConnections(event)
		managerService.commit(event)
		managerService.uncommit(event)

		managerService.wipeData(event)

		ownerService.import(event)
		ownerService.export(event)
		ownerService.exit(event)
	}
}