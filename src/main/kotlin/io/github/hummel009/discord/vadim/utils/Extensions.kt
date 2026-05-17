package io.github.hummel009.discord.vadim.utils

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

fun Guild.getMessageChannelById(id: Long): GuildMessageChannel? = getTextChannelById(id) ?: getThreadChannelById(id)

fun JDA.getMessageChannelById(id: Long): GuildMessageChannel? = getTextChannelById(id) ?: getThreadChannelById(id)