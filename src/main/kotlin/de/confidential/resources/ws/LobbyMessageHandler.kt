package de.confidential.resources.ws

import javax.websocket.Session

interface LobbyMessageHandler<M : LobbyMessage> {

    fun canHandle(): String

    fun handle(session: Session, msg: M): Any
}
