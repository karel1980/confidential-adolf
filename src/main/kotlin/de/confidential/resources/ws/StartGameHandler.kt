package de.confidential.resources.ws

import de.confidential.domain.GamePhase
import de.confidential.domain.Room
import javax.websocket.Session

class StartGameHandler(private val comms: Comms) : RoomMessageHandler<StartGame> {
    override fun canHandle(): String {
        return StartGame::class.toString()
    }

    override fun handle(session: Session, room: Room, msg: StartGame) {
        val game = room.game
        if (game != null && game.phase() != GamePhase.GAME_OVER) {
            throw IllegalArgumentException("Game is still in progress")
        }
        if (room.members.size < 5) {
            throw IllegalStateException("Need at least 5 players to start game")
        }
    }
}
