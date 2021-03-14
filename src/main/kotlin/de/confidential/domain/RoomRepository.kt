package de.confidential.domain

import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class RoomRepository {

    val rooms = mutableMapOf<UUID, Room>()

    fun addRoom(room: Room) {
        rooms[room.id] = room
    }

    fun getAll(): Map<UUID, Room> {
        return rooms;
    }

    fun getRoom(id: UUID): Room {
        return rooms.getOrElse(id) { throw RuntimeException("room not found") }
    }

}
