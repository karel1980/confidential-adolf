package de.confidential.domain

import de.confidential.domain.PolicyTile.FASCIST
import de.confidential.domain.PolicyTile.LIBERAL

class Game(private val players: List<User>) {

    var policyTiles: MutableList<PolicyTile> =
        (List(6) { LIBERAL } + List(11) { FASCIST })
            .shuffled()
            .toMutableList()

    val seats: List<User>

    val liberals: List<User>
    val fascists: List<User>

    val hitler: User

    var currentRound: NormalRound
    val rounds = mutableListOf<Round>()

    val electionTracker = ElectionTracker()

    var enactedLiberalPolicies = 0
    var enactedFasistPolicies = 0

    var winningParty: PolicyTile? = null

    companion object {
        val LIBERAL_POLICIES_NEEDED_TO_WIN = 5
        val FASCIST_POLICIES_NEEDED_TO_WIN = 6
    }

    init {
        seats = players.shuffled()

        val liberalCount = when (seats.size) {
            5 -> 3
            6 -> 4
            7 -> 4
            8 -> 5
            9 -> 5
            10 -> 6
            else -> throw IllegalArgumentException("Unexpected number of players")
        }

        val randomized = seats.shuffled()
        liberals = randomized.take(liberalCount)
        fascists = randomized.takeLast(randomized.size - liberalCount)
        hitler = fascists.random()

        currentRound = NormalRound(0, seats, seats[0])
        rounds.add(currentRound)
    }

    fun presidentialCandidate() = currentRound.presidentialCandidate
    fun chancellor() = currentRound.chancellor
    fun nominateChancellor(nominee: User) {
        if (nominee == presidentialCandidate()) {
            throw IllegalArgumentException("cannnot nominate presidential candidate as chancellor")
        }
        currentRound.chancellor = nominee
    }

    fun voteLeadership(user: User, vote: Vote) {
        currentRound.voteLeadership(user, vote)

        val voteResult = currentRound.leadershipVotingRound.voteResult
        if (voteResult != null) {
            if (voteResult.endResult == Vote.NO) {
                electionTracker.increaseFailedElections()
                if (electionTracker.failedElections == 3) {
                    playChaosRound()
                    if (winningParty != null) {
                        addNormalRoundWithRoundNumber(currentRound.roundNumber + 2)
                    }
                } else {
                    addNormalRound()
                }
            } else {
                electionTracker.resetFailedElections()
                // next round phase: present top 3 cards to president
            }
        }
    }

    private fun addNormalRound() {
        addNormalRoundWithRoundNumber(currentRound.roundNumber + 1)
    }

    private fun addNormalRoundWithRoundNumber(roundNumber: Int) {
        currentRound = NormalRound(
            currentRound.roundNumber + 1, players,
            seats[(seats.indexOf(currentRound.presidentialCandidate) + 1) % seats.size]
        )
        rounds.add(currentRound)
    }

    private fun playChaosRound() {
        rounds.add(ChaosRound(currentRound.roundNumber + 1))
        electionTracker.resetFailedElections()

        val policyTile = drawTopPolicyTile()
        placePolicyTile(policyTile)
        checkGameIsDone()
    }

    private fun placePolicyTile(policyTile: PolicyTile) {
        when (policyTile) {
            FASCIST -> enactedFasistPolicies += 1
            LIBERAL -> enactedLiberalPolicies += 1
        }
    }

    private fun drawTopPolicyTile() = policyTiles.removeAt(0)

    private fun checkGameIsDone(): Boolean {
        if (enactedFasistPolicies == FASCIST_POLICIES_NEEDED_TO_WIN) {
            winningParty = FASCIST
            return true
        }
        if (enactedLiberalPolicies == LIBERAL_POLICIES_NEEDED_TO_WIN) {
            winningParty = LIBERAL
            return true
        }
        return false
    }

    fun leadershipVoteResult(): VoteResult? {
        return currentRound.leadershipVotingRound.voteResult
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
