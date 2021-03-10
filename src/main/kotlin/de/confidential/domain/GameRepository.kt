package de.confidential.domain

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

}
