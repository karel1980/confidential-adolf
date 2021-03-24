package de.confidential.domain

import de.confidential.resources.ws.IncomingMessage
import de.confidential.resources.ws.NominateChancellor
import java.util.*

class NominatingChancellorGamePhaseHandler(val _game: Game) : GamePhaseHandler {

    val state = _game.state

    override fun on(player: UUID, msg: IncomingMessage) {
        if (msg !is NominateChancellor) {
            throw IllegalArgumentException("only accepting nominatechancellor messages")
        }

        val nominee = state.players.find { u -> u.id == msg.nominatedChancellorId }!!

        if (nominee.id == _game.presidentialCandidate().id) {
            throw IllegalArgumentException("presidential candidate cannot be nominated as chancellor")
        }

        _game.state.currentRound.chancellor = nominee
        _game.goToPhase(GamePhase.VOTING_LEADERSHIP)
    }

    override fun getPhase(): GamePhase {
        return GamePhase.NOMINATING_CHANCELLOR
    }

}
