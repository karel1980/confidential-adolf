package de.confidential.resources.ws

import de.confidential.domain.Room
import java.util.function.Consumer
import javax.websocket.Session

class TalkHandler(
    private val comms: Comms
) : RoomMessageHandler<TalkRequest> {

    override fun canHandle() = TalkRequest::class.toString()

    override fun handle(session: Session, room: Room, msg: TalkRequest) {
        broadcast(UserTalked("bob", msg.message))
    }

    private fun broadcast(msg: UserTalked) {
        comms.broadcast(msg)
    }
}
