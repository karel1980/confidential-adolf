package de.confidential.domain

import java.lang.RuntimeException
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserRepository {

    val users = mutableMapOf<UUID, User>()

    fun registerUser(user: User) {
        users[user.id] = user
    }

    fun findUser(id: UUID): User? {
        return users[id]
    }

    fun getUser(userId: UUID): User {
        return findUser(userId) ?: throw RuntimeException("user not found")
    }

}

data class User(val id: UUID, val name: String)
