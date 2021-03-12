package de.confidential.domain

import java.lang.RuntimeException
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class GameRepository {

    val games = mutableMapOf<UUID, Game>()

    fun addGame(game: Game) {
        games.put(game.id, game)
    }

    fun getAll(): Map<UUID, Game> {
        return games;
    }

    fun getGame(id: UUID): Game {
        return games.getOrElse(id) { throw RuntimeException("game not found") }
    }

}
