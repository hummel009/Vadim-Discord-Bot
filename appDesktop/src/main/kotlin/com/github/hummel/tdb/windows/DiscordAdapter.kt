package com.github.hummel.tdb.windows

import com.github.hummel.tdb.core.controller.Controller
import com.github.hummel.tdb.core.controller.impl.ControllerImpl

class DiscordAdapter {
	private val controller: Controller = ControllerImpl()

	fun launch() {
		controller.onCreate()
	}
}