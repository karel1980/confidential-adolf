package de.confidential.domain

import java.util.*

class NormalRound(val roundNumber: Int, val players: List<UUID>, val presidentialCandidate: UUID): Round {

    var chancellor: UUID? = null
    val leadershipVotingRound = VotingRound(players)
    var presidentPolicyTiles: List<PolicyTile>? = null
    var chancellorPolicyTiles: List<PolicyTile>? = null
    var enactedPolicy: PolicyTile? = null
    var vetoRequested = false
    var vetoConfirmed: Boolean? = null

    var performedExecutiveAction: ExecutivePower? = null
    var peekedTiles: List<PolicyTile> = emptyList()
    var investigatedPlayerId: UUID? = null
    var specialElectionPresidentId: UUID? = null
    var executedPlayer: UUID? = null


}
