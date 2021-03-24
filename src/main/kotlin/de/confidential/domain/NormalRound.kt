package de.confidential.domain

class NormalRound(val roundNumber: Int, val players: List<User>, val presidentialCandidate: User): Round {

    var chancellor: User? = null
    val leadershipVotingRound = VotingRound(players)
    var presidentPolicyTiles: List<PolicyTile>? = null
    var chancellorPolicyTiles: List<PolicyTile>? = null
    var enactedPolicy: PolicyTile? = null

}
