package de.confidential.domain

import de.confidential.resources.ws.IncomingMessage
import java.util.*

class ExecutivePowerGamePhaseHandler(game: Game) : GamePhaseHandler {

    private val state = game.state

    override fun on(playerId: UUID, msg: IncomingMessage) {
        //TODO: only allow message types corresponding to presidential action

        if (playerId != state.currentRound.presidentialCandidate.id) {
            throw IllegalArgumentException("Only the president can use executive power")
        }

        //TODO: do we need to check for win states? (e.g. all liberals dead?)
        TODO("handle executive power")

    }

    override fun getPhase(): GamePhase {
        return GamePhase.PRESIDENT_EXECUTIVE_POWER
    }

}
