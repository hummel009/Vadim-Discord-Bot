package com.github.hummel.vadim.factory

import com.github.hummel.vadim.service.*
import com.github.hummel.vadim.service.impl.*

@Suppress("unused", "RedundantSuppression")
object ServiceFactory {
	val loginService: LoginService by lazy { LoginServiceImpl() }
	val memberService: MemberService by lazy { MemberServiceImpl() }
	val managerService: ManagerService by lazy { ManagerServiceImpl() }
	val ownerService: OwnerService by lazy { OwnerServiceImpl() }
	val dataService: DataService by lazy { DataServiceImpl() }
	val accessService: AccessService by lazy { AccessServiceImpl() }
}