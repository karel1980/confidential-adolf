package de.confidential.domain

import de.confidential.domain.PolicyTile.FASCIST
import de.confidential.domain.PolicyTile.LIBERAL
import de.confidential.resources.ws.DiscardPolicyTile
import de.confidential.resources.ws.IncomingMessage
import de.confidential.resources.ws.LeadershipVote
import de.confidential.resources.ws.NominateChancellor
import java.util.*

class Game(_players: List<User>) {

    val state = GameState(_players)

    val phaseHandlers = listOf(
        NominatingChancellorGamePhaseHandler(this),
        VotingLeadershipGamePhaseHandler(this),
        PresidentDiscardsPolicyGamePhaseHandler(this),
        ChancellorDiscardsPolicyGamePhaseHandler(this),
        VetoRequestedGamePhaseHandler(this),
        ExecutivePowerGamePhaseHandler(this),
        GameOverGamePhaseHandler()
    )
        .map { handler -> handler.getPhase() to handler }
        .toMap()

    var phaseHandler: GamePhaseHandler = phaseHandlers[GamePhase.NOMINATING_CHANCELLOR]!!
    fun phase() = phaseHandler.getPhase()

    val players = state.players

    companion object {
        const val LIBERAL_POLICIES_NEEDED_TO_WIN = 5
        const val FASCIST_POLICIES_NEEDED_TO_WIN = 6
    }

    fun presidentialCandidate() = state.currentRound.presidentialCandidate
    fun chancellor() = state.currentRound.chancellor

    fun nominateChancellor(playerId: UUID, nominee: User) {
        this.on(playerId, NominateChancellor(nominee.id))
    }

    fun voteLeadership(playerId: UUID, vote: Vote) {
        this.on(playerId, LeadershipVote(vote))
    }

    fun on(player: UUID, msg: IncomingMessage) {
        if (!(player in players.map { u -> u.id })) {
            throw IllegalArgumentException("$player id is not a player in this game")
        }
        phaseHandler.on(player, msg)
    }

    fun leadershipVoteResult(): VoteResult? {
        return state.currentRound.leadershipVotingRound.voteResult
    }

    fun startNextRound() {
        if (state.electionTracker.failedElections == 3) {
            playChaosRound()
            if (state.winningParty != null) {
                startNextRoundWithRoundNumber(state.currentRound.roundNumber + 2)
            } else {
                goToPhase(GamePhase.GAME_OVER)
            }
        } else {
            startNextRoundWithRoundNumber(state.currentRound.roundNumber + 1)
        }
    }

    private fun startNextRoundWithRoundNumber(roundNumber: Int, nextPresidentId: UUID = determineNextPresidentId()) {
        //TODO: make sure state.policyTiles contains at least three cards.
        // (if < 3 remain, add state.discardedPolicyTiles back to policy tiles and shuffle)

        state.currentRound = NormalRound(
            roundNumber, players,
            //TODO: make next presidential candidate selection should take killed players into account
            players.first { it.id == nextPresidentId }
        )
        state.rounds.add(state.currentRound)
        goToPhase(GamePhase.NOMINATING_CHANCELLOR)
    }

    fun startSpecialElectionRound(nextPresidentId: UUID) {
        startNextRoundWithRoundNumber(state.currentRound.roundNumber + 1, nextPresidentId);
    }

    private fun determineNextPresidentId(): UUID {
        val lastNonSpecialPresident = determineLastNonSpecialPresidentId()
        val lastNonSpecialPresidentIdx = players.indexOfFirst { it -> it.id == lastNonSpecialPresident }
        val firstCandidateIdx = (lastNonSpecialPresidentIdx + 1) % players.size
        val candidates = players.subList(firstCandidateIdx, players.size) + players.subList(0, firstCandidateIdx)
        return candidates.first { it.id !in state.deadPlayers }.id
    }

    private fun determineLastNonSpecialPresidentId(): UUID {
        if (state.rounds.size > 1) {
            val previousRound = state.rounds[state.rounds.size - 2]
            if (roundEndedWithSpecialElection(previousRound)) {
                return (previousRound as NormalRound).presidentialCandidate.id
            }
        }
        return state.currentRound.presidentialCandidate.id
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
        state.rounds.add(ChaosRound(state.currentRound.roundNumber + 1))
        state.electionTracker.resetFailedElections()

        val policyTile = drawTopPolicyTile()
        placePolicyTile(policyTile)
        checkGameIsDone()
    }

    private fun placePolicyTile(policyTile: PolicyTile) {
        when (policyTile) {
            FASCIST -> state.enactedFasistPolicies += 1
            LIBERAL -> state.enactedLiberalPolicies += 1
        }
    }

    private fun drawTopPolicyTile() = state.policyTiles.removeAt(0)

    private fun checkGameIsDone(): Boolean {
        if (state.enactedFasistPolicies == FASCIST_POLICIES_NEEDED_TO_WIN) {
            state.winningParty = FASCIST
            return true
        }
        if (state.enactedLiberalPolicies == LIBERAL_POLICIES_NEEDED_TO_WIN) {
            state.winningParty = LIBERAL
            return true
        }
        return false
    }

    fun presidentPolicyTiles(): List<PolicyTile>? {
        return state.currentRound.presidentPolicyTiles
    }

    fun chancellorPolicyTiles(): List<PolicyTile>? {
        return state.currentRound.chancellorPolicyTiles
    }

    fun discardPolicyTile(user: User, policyToDiscard: PolicyTile) {
        on(user.id, DiscardPolicyTile(policyToDiscard))
    }

    fun goToPhase(phase: GamePhase) {
        this.phaseHandler = phaseHandlers[phase]!!
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
