package de.confidential.resources.ws

import de.confidential.domain.Room
import javax.websocket.Session

class GenericRoomMessageHandler<T:IncomingMessage>(val type: Class<T>): RoomMessageHandler<T> {

    override fun canHandle(): String = type.toString()

    override fun handle(session: Session, room: Room, msg: T) =
        room.game!!.on(SessionUtil.getUserId(session), msg)
}
