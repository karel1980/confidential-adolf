package de.confidential.resources.ws

import de.confidential.domain.User
import de.confidential.domain.UserRepository
import java.util.*
import java.util.UUID.randomUUID
import javax.websocket.Session

class IdentifyHandler(
    val sessions: MutableMap<String, Session>,
    val jsonUtil: JsonUtil,
    val userRepository: UserRepository
) : MessageHandler<IdentifyMessage> {

    override fun canHandle() = IdentifyMessage::class.toString()

    override fun handle(session: Session, msg: IdentifyMessage) {
        createOrUpdateUser(session, msg.id ?: randomUUID(), msg.name)
    }

    private fun createOrUpdateUser(session: Session, id: UUID, name: String): User {
        val user = User(id, name)
        userRepository.registerUser(user)
        SessionUtil.identify(session, user.id)

        val reply = IdentificationSuccess(user.id, user.name)
        session.asyncRemote.sendObject(jsonUtil.asString(reply))
        return user;
    }
}

