package de.confidential.resources.ws

import de.confidential.domain.Room
import de.confidential.domain.RoomRepository
import de.confidential.domain.UserRepository
import java.util.UUID.randomUUID
import javax.websocket.Session

class CreateRoomHandler(
    val sessions: Map<String, Session>,
    val jsonUtil: JsonUtil,
    val userRepository: UserRepository,
    val roomRepository: RoomRepository
) : MessageHandler<CreateRoom> {

    val games = mapOf<String, Room>();

    override fun canHandle() = CreateRoom::class.toString()

    override fun handle(session: Session, msg: CreateRoom) {
        val userId = SessionUtil.getUserId(session)
        val user = userRepository.getUser(userId)

        val room = Room(randomUUID(), user)
        roomRepository.addRoom(room)

        session.asyncRemote.sendObject(jsonUtil.asString(RoomCreated(room.id)))
    }
}
