package de.confidential.domain

import de.confidential.resources.ws.DiscardPolicyTile
import de.confidential.resources.ws.IncomingMessage
import de.confidential.resources.ws.RequestVeto
import java.util.*

class ChancellorDiscardsPolicyGamePhaseHandler(val game: Game) : GamePhaseHandler {

    val state = game.state

    override fun on(playerId: UUID, msg: IncomingMessage) {
        if (playerId != state.currentRound.chancellor!!) {
            throw IllegalArgumentException("Only the chancellor can play right now")
        }

        when(msg) {
            is DiscardPolicyTile -> handleDiscard(msg)
            is RequestVeto -> handleVetoRequest()
            else -> throw IllegalStateException("Only DiscardPolicyTile and RequestVeto are allowed right now")
        }
    }

    fun handleDiscard(msg: DiscardPolicyTile) {
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

        val executiveAction = state.currentExecutiveAction()
        if (executiveAction == null) {
            game.startNextRound()
            game.goToPhase(GamePhase.NOMINATING_CHANCELLOR)
        } else {
            game.goToPhase(GamePhase.PRESIDENT_EXECUTIVE_POWER)
        }
    }

    fun handleVetoRequest() {
        if (!state.vetoPowerUnlocked()) {
            throw IllegalArgumentException("Veto power is not unlocked yet")
        }
        if (state.currentRound.vetoRequested) {
            throw IllegalArgumentException("Veto already requested. Once it's denied you're not allowed to try again")
        }
        state.currentRound.vetoRequested = true
        game.goToPhase(GamePhase.VETO_REQUESTED)
    }

    override fun getPhase(): GamePhase {
        return GamePhase.CHANCELLOR_DISCARDS_POLICY_TILE
    }

}
