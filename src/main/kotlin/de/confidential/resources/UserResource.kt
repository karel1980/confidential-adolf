package de.confidential.resources

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/lobby")
class UserResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello() = "Welcome to the lobby"
}
