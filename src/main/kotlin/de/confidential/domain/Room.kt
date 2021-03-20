package de.confidential.domain

import de.confidential.resources.ws.UserAdded
import java.util.*

class Room(val id: UUID) {

    val members = mutableListOf<User>()

    fun addMember(user: User): UserAdded? {
        if (user in members) {
            return null
        }
        members.add(user)
        return UserAdded(user)
    }

}
