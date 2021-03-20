package de.confidential.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

internal class GameTest {
    @Test
    fun initiallyHas6LiberalAnd11FascistTiles() {
        val game = Game(createUsers(5))

        val fascistCount = game.policyTiles.count { tile -> tile == PolicyTile.FASCIST }
        val liberalCount = game.policyTiles.count { tile -> tile == PolicyTile.LIBERAL }

        assertThat(fascistCount).isEqualTo(11)
        assertThat(liberalCount).isEqualTo(6)
    }

    @Test
    fun assignsMembershipsAndRolesOnCreation() {
        assertAssignments(5, 3, 2)
        assertAssignments(6, 4, 2)
        assertAssignments(7, 4, 3)
        assertAssignments(8, 5, 3)
        assertAssignments(9, 5, 4)
        assertAssignments(10, 6, 4)
    }

    @Test
    fun assignsHitler() {
        val game = Game(createUsers(5))

        assertThat(game.hitler in game.fascists)
    }

    @Test
    fun firstRound_firstSeatBecomesPresidentialCandidate() {
        val game = Game(createUsers(5))

        assertThat(game.presidentialCandidate())
            .isEqualTo(game.seats[0])
    }

    @Test
    fun firstRound_chancellorInitallyNull() {
        val game = Game(createUsers(5))

        assertThat(game.chancellor())
            .isNull()
    }

    @Test
    fun firstRound_nominateChancellorWorks() {
        val game = Game(createUsers(5))
        assertThat(game.chancellor()).isNull()

        game.nominateChancellor(game.seats[1])

        assertThat(game.chancellor()).isEqualTo(game.seats[1])
    }

    private fun assertAssignments(playerCount: Int, expectedLiberals: Int, expectedFascists: Int) {
        val game = Game(createUsers(playerCount))
        assertThat(game.liberals).hasSize(expectedLiberals)
        assertThat(game.fascists).hasSize(expectedFascists)

        assertThat((game.liberals + game.fascists).toSet())
            .hasSize(playerCount)
    }

    fun createUsers(n : Int): List<User> {
        return List(n) { i -> User(randomUUID(), "User $i") }
    }
}
