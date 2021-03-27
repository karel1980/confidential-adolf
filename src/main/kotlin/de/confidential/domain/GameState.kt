package de.confidential.domain

import java.util.*

class GameState(val _players: List<UUID>) {

    val players: List<UUID> = _players.shuffled()
    val deadPlayers = mutableListOf<UUID>()

    var policyTiles: MutableList<PolicyTile> =
        (List(6) { PolicyTile.LIBERAL } + List(11) { PolicyTile.FASCIST })
            .shuffled()
            .toMutableList()
    var discardedPolicyTiles = mutableListOf<PolicyTile>()

    val liberals: List<UUID>
    val fascists: List<UUID>

    private val executivePowersPerFascistPolicy: Map<Int, ExecutivePower?> = when (_players.size) {
        5, 6 -> mapOf(
            Pair(2, ExecutivePower.POLICY_PEEK),
            Pair(3, ExecutivePower.EXECUTION),
            Pair(4, ExecutivePower.EXECUTION))
        7, 8 -> mapOf(
            Pair(1, ExecutivePower.INVESTIGATE_LOYALTY),
            Pair(2, ExecutivePower.CALL_SPECIAL_ELECTION),
            Pair(3, ExecutivePower.EXECUTION),
            Pair(4, ExecutivePower.EXECUTION))
        else -> mapOf(
            Pair(0, ExecutivePower.INVESTIGATE_LOYALTY),
            Pair(1, ExecutivePower.INVESTIGATE_LOYALTY),
            Pair(2, ExecutivePower.CALL_SPECIAL_ELECTION),
            Pair(3, ExecutivePower.EXECUTION),
            Pair(4, ExecutivePower.EXECUTION))
    }

    val hitler: UUID

    var currentRound: NormalRound = NormalRound(1, players, players[0])
    val rounds = mutableListOf<Round>()

    val electionTracker = ElectionTracker()

    var enactedLiberalPolicies = 0
    var enactedFasistPolicies = 0

    fun vetoPowerUnlocked() = enactedFasistPolicies > 4
    fun currentExecutiveAction(): ExecutivePower? {
        if (currentRound.enactedPolicy != PolicyTile.FASCIST) {
            return null
        }
        return executivePowersPerFascistPolicy[this.enactedFasistPolicies - 1]
    }

    var winningParty: PolicyTile? = null

    init {
        val liberalCount = when (players.size) {
            5 -> 3
            6 -> 4
            7 -> 4
            8 -> 5
            9 -> 5
            10 -> 6
            else -> throw IllegalArgumentException("Unexpected number of players")
        }

        executivePowersPerFascistPolicy

        val randomized = players.shuffled()
        liberals = randomized.subList(0, liberalCount)
        fascists = randomized.subList(liberalCount, randomized.size)
        hitler = fascists.random()

        currentRound = NormalRound(0, players, players[0])
        rounds.add(currentRound)
    }

}
