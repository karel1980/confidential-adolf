package de.confidential.domain

import java.security.Principal
import java.util.*

data class GameCreatedEvent(val id: UUID, val owner: String, val isPrivate: Boolean)
