package de.confidential.domain

import de.confidential.domain.GamePhase.CHANCELLOR_DISCARDS_POLICY_TILE
import de.confidential.resources.ws.DiscardPolicyTile
import de.confidential.resources.ws.IncomingMessage
import java.util.*

class PresidentDiscardsPolicyGamePhaseHandler(val game: Game) : GamePhaseHandler {

    val state = game.state

    override fun on(playerId: UUID, msg: IncomingMessage) {
        if (msg !is DiscardPolicyTile) {
            throw IllegalArgumentException("Only discard policy tile allowed right now")
        }

        if (playerId != state.currentRound.presidentialCandidate.id) {
            throw IllegalArgumentException("Only the president can discard a policy")
        }

        val remaining = state.currentRound.presidentPolicyTiles!!.toMutableList()
        if (!remaining.remove(msg.policyToDiscard)) {
            throw IllegalArgumentException("Could not discard policy ${msg.policyToDiscard}")
        }

        state.currentRound.chancellorPolicyTiles = remaining
        state.discardedPolicyTiles.add(msg.policyToDiscard)
        game.goToPhase(CHANCELLOR_DISCARDS_POLICY_TILE)
    }

    override fun getPhase(): GamePhase {
        return GamePhase.PRESIDENT_DISCARDS_POLICY_TILE
    }

}
