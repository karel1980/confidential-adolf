package de.confidential.domain

import de.confidential.domain.ExecutivePower.*
import de.confidential.resources.ws.*
import java.util.*

class ExecutivePowerGameMessageHandler(val game: Game) : GameMessageHandler {

    private val state = game.state

    override fun on(playerId: UUID, msg: IncomingMessage) {
        if (playerId != state.currentRound.presidentialCandidate) {
            throw IllegalArgumentException("Only the president can use executive power")
        }

        when(msg) {
            is InvestigateLoyalty -> investigateLoyalty(msg)
            is CallSpecialElection -> callSpecialElection(msg)
            is PolicyPeek -> policyPeek(msg)
            is Execution -> execution(msg)
            else -> throw IllegalArgumentException("Unsupported message type ${msg.javaClass}")
        }
    }

    private fun investigateLoyalty(msg: InvestigateLoyalty) {
        checkExecutiveAction(INVESTIGATE_LOYALTY)
        state.currentRound.performedExecutiveAction = INVESTIGATE_LOYALTY
        game.state.currentRound.investigatedPlayerId = msg.targetId
        game.startNextRound()
    }

    private fun callSpecialElection(msg: CallSpecialElection) {
        checkExecutiveAction(CALL_SPECIAL_ELECTION)
        state.currentRound.performedExecutiveAction = CALL_SPECIAL_ELECTION
        if (game.presidentialCandidate() == msg.nextPresidentId) {
            throw IllegalArgumentException("President cannot apoint himself in a special election")
        }
        game.state.currentRound.specialElectionPresidentId = msg.nextPresidentId
        game.startSpecialElectionRound(msg.nextPresidentId)
    }

    private fun policyPeek(msg: PolicyPeek) {
        checkExecutiveAction(POLICY_PEEK)
        state.currentRound.performedExecutiveAction = POLICY_PEEK
        state.currentRound.peekedTiles = game.state.policyTiles.take(3)
        game.startNextRound()
    }

    private fun execution(msg: Execution) {
        checkExecutiveAction(EXECUTION)
        if (msg.targetId in state.deadPlayers) {
            throw IllegalArgumentException("He's already dead, Jim")
        }

        state.currentRound.performedExecutiveAction = EXECUTION
        state.currentRound.executedPlayer = msg.targetId
        state.deadPlayers.add(msg.targetId)

        if (state.hitler in state.deadPlayers) {
            game.end(PolicyTile.LIBERAL, "Hitler was killed")
        } else {
            game.startNextRound()
        }
    }

    private fun checkExecutiveAction(action: ExecutivePower) {
        if (game.state.currentExecutiveAction() != action) {
            throw IllegalArgumentException("Only the current executive action ${game.state.currentExecutiveAction()} is allowed")
        }
    }

    override fun getPhase(): GamePhase {
        return GamePhase.PRESIDENT_EXECUTIVE_POWER
    }

}
