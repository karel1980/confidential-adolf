package de.confidential.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.IllegalArgumentException
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
        assertThrows(IllegalArgumentException::class.java, { -> room.startGame() })
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
