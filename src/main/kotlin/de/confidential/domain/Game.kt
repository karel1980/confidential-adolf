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
        ExecutivePowerGamePhaseHandler(this),
        GameOverGamePhaseHandler()
    )
        .map { handler -> handler.getPhase() to handler }
        .toMap()

    var phaseHandler: GamePhaseHandler = phaseHandlers[GamePhase.NOMINATING_CHANCELLOR]!!
    val players = state.players

    companion object {
        val LIBERAL_POLICIES_NEEDED_TO_WIN = 5
        val FASCIST_POLICIES_NEEDED_TO_WIN = 6
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

    fun addNormalRound() {
        addNormalRoundWithRoundNumber(state.currentRound.roundNumber + 1)
    }

    fun addNormalRoundWithRoundNumber(roundNumber: Int) {
        //TODO: make sure state.policyTiles contains at least three cards.
        // (if < 3 remain, add state.discardedPolicyTiles back to policy tiles and shuffle)

        state.currentRound = NormalRound(
            roundNumber, players,
            //TODO: make next presidential candidate selection should take killed players into account
            state.players[(state.players.indexOf(state.currentRound.presidentialCandidate) + 1) % state.players.size]
        )
        state.rounds.add(state.currentRound)
    }

    public fun playChaosRound() {
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
        if (enactedPolicy== LIBERAL) {
            return state.liberalPolicyLane.size == state.enactedLiberalPolicies
        } else {
            return state.fascistPolicyLane.size == state.enactedFasistPolicies
        }
    }

    private fun getPolicyLane(policy: PolicyTile): List<ExecutivePower?> {
        if (policy == LIBERAL) {
            return state.liberalPolicyLane
        } else {
            return state.fascistPolicyLane
        }
    }

    fun getExecutiveAction(): ExecutivePower? {
        val policy = state.currentRound.enactedPolicy!!
        if (policy == LIBERAL) {
            return state.liberalPolicyLane[state.enactedLiberalPolicies]
        }
        return state.fascistPolicyLane[state.enactedFasistPolicies]
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
