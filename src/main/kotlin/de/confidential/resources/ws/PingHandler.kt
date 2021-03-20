package de.confidential.resources.ws

import de.confidential.domain.Room
import javax.websocket.Session

class PingHandler(
    private val comms: Comms
) : RoomMessageHandler<Ping> {

    override fun canHandle() = Ping::class.toString()

    override fun handle(session: Session, room: Room, msg: Ping) {
        comms.sendDirect(Pong(), session)
    }

}

