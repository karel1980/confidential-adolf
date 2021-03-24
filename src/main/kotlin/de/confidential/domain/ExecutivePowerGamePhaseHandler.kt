package de.confidential.domain

import de.confidential.domain.ExecutivePower.*
import de.confidential.resources.ws.*
import java.util.*

class ExecutivePowerGamePhaseHandler(val game: Game) : GamePhaseHandler {

    private val state = game.state

    override fun on(playerId: UUID, msg: IncomingMessage) {
        if (playerId != state.currentRound.presidentialCandidate.id) {
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
        //TODO: do something to communicate the loyalty of the indicated person to the president
        game.startNextRound()
    }

    private fun callSpecialElection(msg: CallSpecialElection) {
        checkExecutiveAction(CALL_SPECIAL_ELECTION)
        if (game.presidentialCandidate().id == msg.nextPresidentId) {
            throw IllegalArgumentException("President cannot apoint himself in a special election")
        }
        //TODO: start a new round with the appointed president
        //TODO: make sure the round after that proceeds with the normal president nomination order
        game.startNextRound()
    }

    private fun policyPeek(msg: PolicyPeek) {
        checkExecutiveAction(POLICY_PEEK)
        //TODO: do something to communicate the peek result to the president
        game.startNextRound()
    }

    private fun execution(msg: Execution) {
        checkExecutiveAction(EXECUTION)
        //TODO: mark the indicated person as dead
        //TODO: if hitler is dead, indicate the winner
        // TODO: fix the code that picks the next president

        game.startNextRound()
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
