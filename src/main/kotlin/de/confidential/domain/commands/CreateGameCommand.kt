package de.confidential.domain.commands

import java.security.Principal

data class CreateGameCommand(val principal: Principal, val isPrivate: Boolean)
