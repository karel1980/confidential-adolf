package de.confidential.domain

import java.util.*

class VotingRound(val voters: List<UUID>) {

    val votes = mutableMapOf<UUID, Vote>()
    var voteResult: VoteResult? = null

    fun registerVote(voter: UUID, vote: Vote) {
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
