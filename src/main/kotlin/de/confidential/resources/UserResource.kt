package de.confidential.resources

import de.confidential.domain.UserRepository
import java.util.*
import java.util.UUID.randomUUID
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.SecurityContext

@Path("/user")
class UserResource {

    @Inject
    @field: Default
    lateinit var repo: UserRepository;

    @GET()
    @Path("token")
    @Produces(MediaType.APPLICATION_JSON)
    fun getToken(@Context sec: SecurityContext): TokenResponse {
        val user = sec.userPrincipal.toString()
        if (!repo.hasToken(user)) {
            val token = randomUUID()
            repo.registerToken(user, token)
            return TokenResponse(token)
        }

        return TokenResponse(repo.getUserToken(user))
    }

    data class TokenResponse(val token: UUID)
}

