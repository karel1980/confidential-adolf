package de.confidential.resources.ws

import de.confidential.domain.Room
import javax.websocket.Session

class RestartGameHandler() : RoomMessageHandler<StartGame> {
    override fun canHandle(): String {
        return StartGame::class.toString()
    }

    override fun handle(session: Session, room: Room, msg: StartGame) {
        room.startGame()
    }
}
