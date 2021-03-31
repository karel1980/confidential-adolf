package de.confidential.resources.ws

import de.confidential.domain.Room
import javax.websocket.Session

class RestartGameHandler() : RoomMessageHandler<RestartGame> {
    override fun canHandle(): String {
        return RestartGame::class.toString()
    }

    override fun handle(session: Session, room: Room, msg: RestartGame) {
        room.startGame()
    }
}
