package de.confidential.domain

import de.confidential.resources.ws.IncomingMessage
import java.util.*

interface GameMessageHandler {

    fun on(playerId: UUID, msg: IncomingMessage)
    fun getPhase(): GamePhase

}
