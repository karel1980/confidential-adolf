package de.confidential.domain

class NormalRound(val roundNumber: Int, val players: List<User>, val presidentialCandidate: User): Round {

    var chancellor: User? = null
    val leadershipVotingRound = VotingRound(players)
    var presidentPolicyTiles: List<PolicyTile>? = null
    var chancellorPolicyTiles: List<PolicyTile>? = null

    fun voteLeadership(user: User, vote: Vote) {
        if (chancellor == null) {
            throw IllegalStateException("Voting not allowed when chancellor is not yet nominated")
        }
        leadershipVotingRound.registerVote(user, vote)
    }

}
