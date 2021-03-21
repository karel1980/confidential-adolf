package de.confidential.domain

class NormalRound(val roundNumber: Int, val players: List<User>, val presidentialCandidate: User): Round {

    var chancellor: User? = null
    val leadershipVotingRound = VotingRound(players)

    fun voteLeadership(user: User, vote: Vote) {
        leadershipVotingRound.registerVote(user, vote)
    }

}
