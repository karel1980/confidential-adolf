package de.confidential.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import java.util.UUID.randomUUID

@ExtendWith(MockitoExtension::class)
internal class RoomTest {

    @Test
    fun constructor_createsRoomWithoutGame() {
        val room = Room(randomUUID())
        assertThat(room.game).isNull()
    }

    @Test
    fun roomTo_hitlerIsToldHeIsHitler() {
        val room = Room(randomUUID())
        createUsers(5).forEach { room.addMember(it) }
        room.startGame()

        val hitlerId = room.game!!.state.hitler
        val roomTO = room.getState(hitlerId)
        assertThat(roomTO.game!!.players.find { it.id == hitlerId }!!.hitler)
            .isTrue

    }

    private fun createUsers(n: Int): List<User> {
        return List(n) { User(randomUUID(), "User $it") }
    }

    @Test
    fun addMember() {
        val room = Room(randomUUID())
        val bob = User(randomUUID(), "bob")

        room.addMember(bob)

        assertThat(room.members)
            .contains(bob)
    }

    @Test
    fun startGame_throwsExceptionIfInsufficientPlayers() {
        val room = Room(randomUUID())
        assertThrows(IllegalArgumentException::class.java) { -> room.startGame() }
    }

    @Test
    fun startGame_startsGame() {
        val room = Room(randomUUID())
        assertThat(room.game).isNull()
        repeat(5) { i -> room.addMember(User(UUID.randomUUID(), "Player $i")) }

        room.startGame()

        assertThat(room.game).isNotNull
    }

}
