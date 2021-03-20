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

    var currentRound: Round

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

        currentRound = Round(0, seats[0])
    }

    fun presidentialCandidate() = currentRound.presidentialCandidate
    fun chancellor() = currentRound.chancellor
    fun nominateChancellor(nominee: User) {
        if (nominee == presidentialCandidate()) {
            throw IllegalArgumentException("cannnot nominate presidential candidate as chancellor")
        }
        currentRound.chancellor = nominee
    }
}
