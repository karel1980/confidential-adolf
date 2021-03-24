package de.confidential.domain

import de.confidential.resources.ws.IncomingMessage
import de.confidential.resources.ws.LeadershipVote
import java.lang.IllegalArgumentException
import java.util.*

class VotingLeadershipGamePhaseHandler(val game: Game) : GamePhaseHandler {

    val state = game.state

    override fun on(playerId: UUID, msg: IncomingMessage) {
        if (msg !is LeadershipVote) {
            throw IllegalArgumentException("Only LeadershipVote messages accepted")
        }

        val player = state.players.find { p -> p.id == playerId }!!
        state.currentRound.leadershipVotingRound.registerVote(player, msg.vote)

        val voteResult = state.currentRound.leadershipVotingRound.voteResult ?: return

        if (voteResult.endResult == Vote.NO) {
            state.electionTracker.increaseFailedElections()
            game.startNextRound()
        } else {
            state.electionTracker.resetFailedElections()
            state.currentRound.presidentPolicyTiles = state.policyTiles.take(3)
            state.policyTiles = state.policyTiles.subList(3, state.policyTiles.size)
            game.goToPhase(GamePhase.PRESIDENT_DISCARDS_POLICY_TILE)
        }
    }

    override fun getPhase(): GamePhase {
        return GamePhase.VOTING_LEADERSHIP
    }

}
