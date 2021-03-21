package de.confidential.domain

class VotingRound(val voters: List<User>) {

    val votes = mutableMapOf<User, Vote>()
    var voteResult: VoteResult? = null

    fun registerVote(voter: User, vote: Vote) {
        if (voter in votes.keys) {
            throw AlreadyVotedException()
        }
        if (!(voter in voters)) {
            throw NonPlayersCannotVoteException()
        }

        votes[voter] = vote

        if (votes.size == voters.size) {
            voteResult = VoteResult.create(votes.values)
        }
    }
}
