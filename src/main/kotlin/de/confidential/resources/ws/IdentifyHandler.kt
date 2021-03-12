package de.confidential.resources.ws

import de.confidential.domain.UserRepository
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.websocket.Session

class IdentifyHandler(val sessions: MutableMap<String, Session>) : LobbyMessageHandler<IdentifyMessage> {

    @Inject
    @field: Default
    lateinit var repo: UserRepository

    override fun canHandle() = IdentifyMessage::class.toString()

    override fun handle(session: Session, msg: IdentifyMessage) {
        SessionUtil.identify(session, repo.findUserByToken(msg.token))
    }
}

