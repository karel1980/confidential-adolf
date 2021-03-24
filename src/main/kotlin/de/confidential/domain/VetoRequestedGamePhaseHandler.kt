package de.confidential.domain

import de.confidential.domain.GamePhase.CHANCELLOR_DISCARDS_POLICY_TILE
import de.confidential.domain.GamePhase.VETO_REQUESTED
import de.confidential.resources.ws.ConfirmVeto
import de.confidential.resources.ws.DenyVeto
import de.confidential.resources.ws.IncomingMessage
import java.util.*

class VetoRequestedGamePhaseHandler(val game: Game) : GamePhaseHandler {

    val state = game.state

    override fun on(playerId: UUID, msg: IncomingMessage) {
        if (playerId != state.currentRound.presidentialCandidate.id) {
            throw IllegalArgumentException("Only the president is allowed to act right now")
        }

        when (msg) {
            is ConfirmVeto -> confirmVeto()
            is DenyVeto -> denyVeto()
            else -> IllegalArgumentException("Only confirm veto or deny veto actions are available right now")
        }

    }

    override fun getPhase(): GamePhase {
        return VETO_REQUESTED
    }

    fun confirmVeto() {
        state.currentRound.vetoConfirmed = true
        state.electionTracker.increaseFailedElections()
        game.startNextRound()
    }

    fun denyVeto() {
        state.currentRound.vetoConfirmed = false
        game.goToPhase(CHANCELLOR_DISCARDS_POLICY_TILE)
    }

}
