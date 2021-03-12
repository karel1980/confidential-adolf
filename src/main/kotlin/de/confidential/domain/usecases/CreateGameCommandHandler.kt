package de.confidential.domain.usecases

import de.confidential.domain.Game
import de.confidential.domain.GameCreatedEvent
import de.confidential.domain.GameRepository
import de.confidential.domain.UserRepository
import de.confidential.resources.ws.CreateGameMessage
import de.confidential.resources.ws.LobbyMessageHandler
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.websocket.Session

class CreateGameCommandHandler: LobbyMessageHandler<CreateGameMessage> {

    @Inject
    @field: Default
    lateinit var repo: GameRepository;

    @Inject
    @field: Default
    lateinit var userRepository: UserRepository;

    override fun canHandle() = CreateGameMessage::class.toString()

    override fun handle(session: Session, msg: CreateGameMessage): GameCreatedEvent {
        val user = session.userProperties.getOrDefault("USER_ID") {
            throw RuntimeException("please identify")
        } as String
        val game = Game(user, msg.isPrivate)

        repo.addGame(game)

        return GameCreatedEvent(game.id, owner = game.owner, game.isPrivate)
    }

}
