package de.confidential.domain

import de.confidential.resources.ws.IncomingMessage
import java.util.*

interface GamePhaseHandler {

    fun on(playerId: UUID, msg: IncomingMessage)
    fun getPhase(): GamePhase

}
