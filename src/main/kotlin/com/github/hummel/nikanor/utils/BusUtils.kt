package com.github.hummel.nikanor.utils

fun getDiscordAuthorName(message: net.dv8tion.jda.api.entities.Message): String =
	message.author.effectiveName.replace("  ", " ").replace(" ", "_")

fun getTelegramAuthorName(message: org.telegram.telegrambots.meta.api.objects.message.Message): String? {
	var author = message.from.userName

	if (author == null) {
		author = message.from.firstName
		if (message.from.lastName != null) {
			author += "_" + message.from.lastName
		}
	}
	return author.replace("  ", " ").replace(" ", "_")
}