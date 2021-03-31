package de.confidential.domain

import de.confidential.resources.ws.IncomingMessage
import de.confidential.resources.ws.NominateChancellor
import java.util.*

class NominatingChancellorGameMessageHandler(val _game: Game) : GameMessageHandler {

    val state = _game.state

    override fun on(playerId: UUID, msg: IncomingMessage) {
        if (playerId != state.currentRound.presidentialCandidate) {
            throw IllegalArgumentException("only the president can nominate a chancellor")
        }
        if (msg !is NominateChancellor) {
            throw IllegalArgumentException("only accepting nominatechancellor messages")
        }

        val nominee = state.players.find { it == msg.nominatedChancellorId }!!

        if (nominee == _game.presidentialCandidate()) {
            throw IllegalArgumentException("presidential candidate cannot be nominated as chancellor")
        }

        _game.state.currentRound.chancellor = nominee
        _game.goToPhase(GamePhase.VOTING_LEADERSHIP)
    }

    override fun getPhase(): GamePhase {
        return GamePhase.NOMINATING_CHANCELLOR
    }

}
