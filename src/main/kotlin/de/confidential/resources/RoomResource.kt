package de.confidential.resources

import de.confidential.domain.Room
import de.confidential.domain.RoomRepository
import de.confidential.resources.ws.RoomCreated
import java.util.UUID.randomUUID
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/api/room")
class RoomResource {

    @Inject
    @field: Default
    lateinit var roomRepository: RoomRepository;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun createRoom(): RoomCreated {
        val room = Room(randomUUID())
        roomRepository.addRoom(room)

        return RoomCreated(room.id)
    }

}
