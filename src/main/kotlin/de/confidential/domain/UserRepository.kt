package de.confidential.domain

import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserRepository {

    val users = mutableMapOf<UUID, User>()

    fun registerUser(user: User) {
        users[user.id] = user
    }

    fun findUserById(id: UUID): User? {
        return users[id]
    }

}

data class User(val id: UUID, val name: String)
