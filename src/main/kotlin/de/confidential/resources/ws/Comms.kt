package de.confidential.resources.ws

import de.confidential.domain.Room
import java.util.*
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.inject.Singleton
import javax.websocket.Session

@Singleton
class Comms {

    @Inject
    @field: Default
    lateinit var jsonUtil: JsonUtil

    val sessionsById = mutableMapOf<String, Session>()

    //TODO: replace with guava multimap, will make this class simpler
    val sessionsByRoomId = mutableMapOf<UUID, MutableList<Session>>();

    fun sendDirect(msg: OutgoingMessage, session: Session) {
        session.asyncRemote.sendObject(jsonUtil.asString(msg))
    }

    fun addSession(room: Room, session: Session) {
        sessionsById[session.id] = session;
        sessionsByRoomId
            .getOrPut(room.id, { -> mutableListOf() })
            .add(session)
    }

    fun removeSession(id: String?) {
        val session = sessionsById.remove(id) ?: return
        val room = session.userProperties["room"] as Room? ?: return
        sessionsByRoomId.remove(room.id)
    }

    fun sendToAllMembers(msg: OutgoingMessage, roomId: UUID) {
        sessionsByRoomId[roomId]?.forEach { session ->
            sendDirect(msg, session)
        }
    }

    fun broadcast(msg: OutgoingMessage) {
        this.sessionsById.values.forEach { session ->
            sendDirect(msg, session)
        }
    }

}
