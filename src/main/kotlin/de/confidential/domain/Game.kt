package de.confidential.domain

import de.confidential.domain.PolicyTile.FASCIST
import de.confidential.domain.PolicyTile.LIBERAL
import de.confidential.resources.ws.DiscardPolicyTile
import de.confidential.resources.ws.IncomingMessage
import de.confidential.resources.ws.LeadershipVote
import de.confidential.resources.ws.NominateChancellor
import java.util.*

class Game(_players: List<UUID>) {

    val state = GameState(_players)

    val phaseHandlers = listOf(
        NominatingChancellorGameMessageHandler(this),
        VotingLeadershipGameMessageHandler(this),
        PresidentDiscardsPolicyGameMessageHandler(this),
        ChancellorDiscardsPolicyGameMessageHandler(this),
        VetoRequestedGameMessageHandler(this),
        ExecutivePowerGameMessageHandler(this),
        GameOverGameMessageHandler()
    )
        .map { handler -> handler.getPhase() to handler }
        .toMap()

    var messageHandler: GameMessageHandler = phaseHandlers[GamePhase.NOMINATING_CHANCELLOR]!!
    fun phase() = messageHandler.getPhase()

    val players = state.players

    companion object {
        const val LIBERAL_POLICIES_NEEDED_TO_WIN = 5
        const val FASCIST_POLICIES_NEEDED_TO_WIN = 6
    }

    fun presidentialCandidate() = state.currentRound.presidentialCandidate
    fun chancellor() = state.currentRound.chancellor

    fun nominateChancellor(playerId: UUID, nominee: UUID) {
        this.on(playerId, NominateChancellor(nominee))
    }

    fun voteLeadership(playerId: UUID, vote: Vote) {
        this.on(playerId, LeadershipVote(vote))
    }

    fun on(player: UUID, msg: IncomingMessage) {
        if (!(player in players)) {
            throw IllegalArgumentException("$player id is not a player in this game")
        }
        messageHandler.on(player, msg)
    }

    fun leadershipVoteResult(): VoteResult? {
        return state.currentRound.leadershipVotingRound.voteResult
    }

    fun startNextRound() {
        if (state.electionTracker.failedElections == 3) {
            playChaosRound()
        } else {
            startNextRoundWithRoundNumber(state.currentRound.roundNumber + 1)
        }
    }

    private fun startNextRoundWithRoundNumber(roundNumber: Int, nextPresidentId: UUID = determineNextPresidentId()) {
        if (nextPresidentId in state.deadPlayers) {
            throw IllegalStateException("Next round would start with a dead president. This should never ever happen, and your game is probably in a bad state.")
        }
        restockPolicyTiles()

        state.currentRound = NormalRound(
            roundNumber, players, nextPresidentId
        )

        state.rounds.add(state.currentRound)

        if (state.enactedFasistPolicies > 3 && state.hitler == state.currentRound.presidentialCandidate) {
            end(FASCIST, "Hitler became president after more than 3 enacted fascist policies")
        } else {
            goToPhase(GamePhase.NOMINATING_CHANCELLOR)
        }
    }

    private fun restockPolicyTiles() {
        if (state.policyTiles.size < 3) {
            state.policyTiles = (state.policyTiles + state.discardedPolicyTiles).shuffled().toMutableList()
            state.discardedPolicyTiles = mutableListOf()
        }
    }

    fun startSpecialElectionRound(nextPresidentId: UUID) {
        startNextRoundWithRoundNumber(state.currentRound.roundNumber + 1, nextPresidentId);
    }

    private fun determineNextPresidentId(): UUID {
        val lastNonSpecialPresident = determineLastNonSpecialPresidentId()
        val lastNonSpecialPresidentIdx = players.indexOf(lastNonSpecialPresident)
        val firstCandidateIdx = (lastNonSpecialPresidentIdx + 1) % players.size
        val candidates = players.subList(firstCandidateIdx, players.size) + players.subList(0, firstCandidateIdx)
        return candidates.first { it !in state.deadPlayers }
    }

    private fun determineLastNonSpecialPresidentId(): UUID {
        if (state.rounds.size > 1) {
            val previousRound = state.rounds[state.rounds.size - 2]
            if (roundEndedWithSpecialElection(previousRound)) {
                return (previousRound as NormalRound).presidentialCandidate
            }
        }
        return state.currentRound.presidentialCandidate
    }

    private fun roundEndedWithSpecialElection(round: Round): Boolean {
        if (round is NormalRound) {
            if (round.performedExecutiveAction == ExecutivePower.CALL_SPECIAL_ELECTION) {
                return true
            }
        }
        return false
    }

    private fun playChaosRound() {
        restockPolicyTiles()
        state.rounds.add(ChaosRound(state.currentRound.roundNumber + 1))
        state.electionTracker.resetFailedElections()

        val policyTile = drawTopPolicyTile()
        placePolicyTile(policyTile)
        if (facistLaneIsFull()) {
            end(FASCIST, "6 fascist policies enacted")
        }
        if (liberalLaneIsFull()) {
            end(LIBERAL, "5 liberal policies enacted")
        }
        startNextRoundWithRoundNumber(state.currentRound.roundNumber + 2)

    }

    private fun placePolicyTile(policyTile: PolicyTile) {
        when (policyTile) {
            FASCIST -> state.enactedFasistPolicies += 1
            LIBERAL -> state.enactedLiberalPolicies += 1
        }
    }

    private fun drawTopPolicyTile() = state.policyTiles.removeAt(0)
    private fun facistLaneIsFull() = state.enactedFasistPolicies == FASCIST_POLICIES_NEEDED_TO_WIN
    private fun liberalLaneIsFull() = state.enactedLiberalPolicies == LIBERAL_POLICIES_NEEDED_TO_WIN

    fun presidentPolicyTiles(): List<PolicyTile>? {
        return state.currentRound.presidentPolicyTiles
    }

    fun chancellorPolicyTiles(): List<PolicyTile>? {
        return state.currentRound.chancellorPolicyTiles
    }

    fun discardPolicyTile(user: UUID, policyToDiscard: PolicyTile) {
        on(user, DiscardPolicyTile(policyToDiscard))
    }

    fun goToPhase(phase: GamePhase) {
        this.messageHandler = phaseHandlers[phase]!!
    }

    fun allPoliciesEnacted(enactedPolicy: PolicyTile): Boolean {
        if (enactedPolicy == LIBERAL) {
            return state.enactedLiberalPolicies == LIBERAL_POLICIES_NEEDED_TO_WIN
        } else {
            return state.enactedFasistPolicies == FASCIST_POLICIES_NEEDED_TO_WIN
        }
    }

    fun previousRound(): Round {
        return state.rounds[state.rounds.size-2]
    }

    fun end(winner: PolicyTile, reason: String) {
        state.winningParty = winner
        state.winReason = reason
        goToPhase(GamePhase.GAME_OVER)
    }

}

data class VoteResult(val yesVotes: Int, val noVotes: Int) {

    val endResult: Vote = if (yesVotes > noVotes) {
        Vote.YES
    } else {
        Vote.NO
    }

    companion object {
        fun create(votes: Collection<Vote>): VoteResult {
            return VoteResult(votes.count { v -> v == Vote.YES }, votes.count { v -> v == Vote.NO })
        }
    }

}
