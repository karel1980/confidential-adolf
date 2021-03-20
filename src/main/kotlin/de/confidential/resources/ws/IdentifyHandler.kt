package de.confidential.resources.ws

import de.confidential.domain.Room
import de.confidential.domain.User
import de.confidential.domain.UserRepository
import java.util.*
import java.util.UUID.randomUUID
import javax.websocket.Session

class IdentifyHandler(
    private val comms: Comms,
    private val userRepository: UserRepository
) : RoomMessageHandler<IdentifyMessage> {

    override fun canHandle() = IdentifyMessage::class.toString()

    override fun handle(session: Session, room: Room, msg: IdentifyMessage) {
        val user = createOrUpdateUser(session, msg.id?: randomUUID(), msg.name)

        comms.sendDirect(IdentificationSuccess(user.id, user.name), session)

        // TODO: Just publish the event and let event handler dispatch it
        val added = room.addMember(user)
        if (added != null) {
            comms.sendToAllMembers(added, room.id)
        }
    }

    private fun createOrUpdateUser(session: Session, id: UUID, name: String): User {
        val user = User(id, name)
        userRepository.registerUser(user)
        SessionUtil.identify(session, user.id)
        return user;
    }
}

