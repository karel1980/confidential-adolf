package de.confidential.domain

class GameState(val _players: List<User>) {

    val players: List<User> = _players.shuffled()

    var policyTiles: MutableList<PolicyTile> =
        (List(6) { PolicyTile.LIBERAL } + List(11) { PolicyTile.FASCIST })
            .shuffled()
            .toMutableList()
    var discardedPolicyTiles = mutableListOf<PolicyTile>()

    val liberals: List<User>
    val fascists: List<User>

    val liberalPolicyLane: List<ExecutivePower?> = when(_players.size) {
        5,6 -> listOf(null, null, null, null, null, null)
        7,8 -> listOf(null, null, null, null, null, null)
        else -> listOf(null, null, null, null, null, null)
    }
    val fascistPolicyLane: List<ExecutivePower?> = when(_players.size) {
        5,6 -> listOf(null, null, null, null, null, null)
        7,8 -> listOf(null, null, null, null, null, null)
        else -> listOf(null, null, null, null, null, null)
    }

    val hitler: User

    var currentRound: NormalRound = NormalRound(1, players, players[0])
    val rounds = mutableListOf<Round>()

    val electionTracker = ElectionTracker()

    var enactedLiberalPolicies = 0
    var enactedFasistPolicies = 0

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

        val randomized = players.shuffled()
        liberals = randomized.subList(0, liberalCount)
        fascists = randomized.subList(liberalCount, randomized.size)
        hitler = fascists.random()

        currentRound = NormalRound(0, players, players[0])
        rounds.add(currentRound)
    }

}
