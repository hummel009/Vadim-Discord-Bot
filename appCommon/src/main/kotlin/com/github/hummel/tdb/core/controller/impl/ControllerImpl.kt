package com.github.hummel.tdb.core.controller.impl

import com.github.hummel.tdb.core.controller.Controller
import com.github.hummel.tdb.core.factory.ServiceFactory
import com.github.hummel.tdb.core.service.LoginService

class ControllerImpl : Controller {
	private val loginService: LoginService = ServiceFactory.loginService

	override fun onCreate() {
		loginService.loginBot()
	}
}