package de.confidential.domain.usecases

import de.confidential.domain.ChatRepository
import de.confidential.domain.Game
import de.confidential.domain.GameCreatedEvent
import de.confidential.domain.GameRepository
import de.confidential.domain.commands.CreateGameCommand
import java.util.*
import javax.enterprise.inject.Default
import javax.inject.Inject

class CreateGameCommandHandler {

    @Inject
    @field: Default
    lateinit var repo: GameRepository;

    fun handle(cmd: CreateGameCommand): GameCreatedEvent {
        val game = Game(cmd.principal, cmd.isPrivate)

        repo.addGame(game)

        return GameCreatedEvent(game.id, owner = game.owner.toString(), game.isPrivate)
    }

}
