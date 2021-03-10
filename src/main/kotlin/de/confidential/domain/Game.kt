package de.confidential.domain

import java.security.Principal
import java.util.*

class Game(val owner: Principal, val isPrivate: Boolean) {

    val id: UUID;

    init {
        id = UUID.randomUUID();
    }
}
