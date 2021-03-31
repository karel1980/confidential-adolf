package de.confidential.domain

import de.confidential.resources.ws.IncomingMessage
import java.util.*

class GameOverGameMessageHandler() : GameMessageHandler {

    override fun on(playerId: UUID, msg: IncomingMessage) {
        throw IllegalArgumentException("Game over. Not accepting new messages")
    }

    override fun getPhase(): GamePhase {
        return GamePhase.GAME_OVER
    }

}
