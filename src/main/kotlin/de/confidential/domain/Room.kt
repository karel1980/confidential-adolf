package de.confidential.domain

import java.util.*

class Room(val id: UUID, val creator: User) {

    val spectators = listOf<User>();

}
