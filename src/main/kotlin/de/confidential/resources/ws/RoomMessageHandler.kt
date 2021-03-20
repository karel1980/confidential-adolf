package de.confidential.resources.ws

import de.confidential.domain.Room
import javax.websocket.Session

interface RoomMessageHandler<M : IncomingMessage> {

    fun canHandle(): String

    fun handle(session: Session, room: Room, msg: M)
}
