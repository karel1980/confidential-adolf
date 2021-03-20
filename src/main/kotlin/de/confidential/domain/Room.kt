package de.confidential.domain

import de.confidential.resources.ws.UserAdded
import java.util.*

class Room(val id: UUID) {

    var game: Game? = null

    companion object {
        val MIN_PLAYERS = 5;
        val MAX_PLAYERS = 10;
    }

    val members = mutableListOf<User>()

    fun addMember(user: User): UserAdded? {
        if (user in members) {
            return null
        }
        members.add(user)
        return UserAdded(user)
    }

    fun startGame() {
        if (members.size < MIN_PLAYERS) {
            throw IllegalArgumentException("Not enough players")
        }
        if (members.size > MAX_PLAYERS){
            throw IllegalArgumentException("Too many players")
        }

        this.game = Game(members.map{it}.toList())
    }

}
