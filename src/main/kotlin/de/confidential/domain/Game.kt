package de.confidential.domain

import java.util.*

class Game(val owner: String, val isPrivate: Boolean) {

    val id: UUID;

    init {
        id = UUID.randomUUID();
    }
}
