package de.confidential.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.UUID.randomUUID

@ExtendWith(MockitoExtension::class)
internal class RoomTest {

    @Test
    fun addUser() {
        val room = Room(randomUUID())
        val bob = User(randomUUID(), "bob")

        room.addMember(bob)

        assertThat(room.members)
            .contains(bob)
    }
}
