package com.github.hummel.nikanor.utils

import com.github.hummel.nikanor.bean.GuildData
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed

fun EmbedBuilder.error(member: Member?, guildData: GuildData, desc: String): MessageEmbed = apply {
	member ?: return@apply

	setAuthor(member.effectiveName, null, member.effectiveAvatarUrl)
	setTitle(I18n.of("title_error", guildData))
	setDescription(desc)
	setColor(0xFF0000)
}.build()

fun EmbedBuilder.access(member: Member?, guildData: GuildData, desc: String): MessageEmbed = apply {
	member ?: return@apply

	setAuthor(member.effectiveName, null, member.effectiveAvatarUrl)
	setTitle(I18n.of("title_access", guildData))
	setDescription(desc)
	setColor(0xFFFF00)
}.build()

fun EmbedBuilder.success(member: Member?, guildData: GuildData, desc: String): MessageEmbed = apply {
	member ?: return@apply

	setAuthor(member.effectiveName, null, member.effectiveAvatarUrl)
	setTitle(I18n.of("title_success", guildData))
	setDescription(desc)
	setColor(0x00FF00)
}.build()