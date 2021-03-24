package de.confidential.domain

import de.confidential.resources.ws.DiscardPolicyTile
import de.confidential.resources.ws.IncomingMessage
import java.util.*

class ChancellorDiscardsPolicyGamePhaseHandler(val game: Game) : GamePhaseHandler {

    val state = game.state

    override fun on(playerId: UUID, msg: IncomingMessage) {
        if (msg !is DiscardPolicyTile) {
            throw IllegalArgumentException("Only discard policy tile allowed right now")
        }

        if (playerId != state.currentRound.chancellor!!.id) {
            throw IllegalArgumentException("Only the chancellor can discard a policy right now")
        }

        val remaining = state.currentRound.chancellorPolicyTiles!!.toMutableList()
        if (!remaining.remove(msg.policyToDiscard)) {
            throw IllegalArgumentException("Could not discard policy ${msg.policyToDiscard}")
        }
        val enactedPolicy = remaining[0]
        state.currentRound.enactedPolicy = enactedPolicy
        state.discardedPolicyTiles.add(msg.policyToDiscard)

        if (enactedPolicy == PolicyTile.LIBERAL) {
            state.enactedLiberalPolicies += 1
        } else {
            state.enactedFasistPolicies += 1
        }

        if (game.allPoliciesEnacted(enactedPolicy)) {
            game.goToPhase(GamePhase.GAME_OVER)
            state.winningParty = enactedPolicy
            return
        }

        val executiveAction = game.getExecutiveAction()
        if (executiveAction == null) {
            game.addNormalRound()
            game.goToPhase(GamePhase.NOMINATING_CHANCELLOR)
        } else {
            game.goToPhase(GamePhase.PRESIDENT_EXECUTIVE_POWER)
        }
    }

    override fun getPhase(): GamePhase {
        return GamePhase.CHANCELLOR_DISCARDS_POLICY_TILE
    }

}
