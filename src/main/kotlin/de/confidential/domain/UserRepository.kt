package de.confidential.domain

import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserRepository {

    //TODO: allow tokens to expire

    val tokenByUser: MutableMap<String, UUID> = mutableMapOf()
    val userByToken: MutableMap<UUID, String> = mutableMapOf()

    fun registerToken(user: String, token: UUID) {
        tokenByUser.put(user, token)
        userByToken.put(token, user);
    }

    fun getUserToken(user: String): UUID {
        return tokenByUser.getOrElse(user) {
            throw RuntimeException("no token found for user")
        }
    }

    fun findUserByToken(token: UUID): String {
        return userByToken.getOrElse(token) {
            throw RuntimeException("no user found for token")
        }
    }

    fun hasToken(user: String): Boolean {
        return tokenByUser.containsKey(user);
    }

}
