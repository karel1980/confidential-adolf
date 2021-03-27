package de.confidential.resources.ws

import de.confidential.domain.Room
import javax.websocket.Session

class GetRoomStateHandler(private val comms: Comms): RoomMessageHandler<GetRoomState> {
    override fun canHandle(): String {
        return GetRoomState::class.toString()
    }

    override fun handle(session: Session, room: Room, msg: GetRoomState) {
        val user = SessionUtil.getUserId(session)
        comms.sendDirect(room.getState(user), session)
    }
}
