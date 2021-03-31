package de.confidential.domain

import de.confidential.resources.ws.*
import java.util.*

class Room(val id: UUID) {

    var game: Game? = null

    companion object {
        const val MIN_PLAYERS = 5;
        const val MAX_PLAYERS = 10;
    }

    val members = mutableListOf<User>()

    fun addMember(user: User): UserAdded? {
        if (members.any { u -> u.id == user.id }) {
            return null
        }
        members.add(user)
        return UserAdded(user)
    }

    fun startGame() {
        if (members.size < MIN_PLAYERS) {
            throw IllegalArgumentException("Not enough players")
        }
        if (members.size > MAX_PLAYERS) {
            throw IllegalArgumentException("Too many players")
        }

        this.game = Game(members.map { it.id }.toList())
    }

    fun getState(userId: UUID): RoomState {
        return RoomState(
            members, if (game == null) {
                null
            } else {
                gameTO(game!!, userId)
            }
        )
    }

    private fun gameTO(game: Game, userId: UUID): GameTO {
        return GameTO(
            game.players.map {
                PlayerTO(
                    it,
                    members.first { member -> member.id == it }.name,
                    it in game.state.deadPlayers,
                    if (it == userId || game.phase() == GamePhase.GAME_OVER) {
                        val hitler = game.state.hitler.equals(it)
                        println("$it hitler? $hitler")
                        hitler
                    } else {
                        null
                    }
                )
            },
            game.state.rounds.map { roundTo(it, userId) },
            game.state.electionTracker.failedElections,
            game.state.enactedLiberalPolicies,
            game.state.enactedFasistPolicies,
            game.phase(),
            game.state.winningParty,
            List(Game.FASCIST_POLICIES_NEEDED_TO_WIN) {
                PolicyLaneTileTO(game.state.executivePowersPerFascistPolicy[it])
            }
        )
    }

    private fun roundTo(round: Round, userId: UUID): RoundTO = when (round) {
        is NormalRound ->
            normalRoundTo(round, userId)
        is ChaosRound ->
            ChaosRoundTO(round.roundNumber)
        else -> throw java.lang.IllegalArgumentException("${round.javaClass} not supported here")
    }

    private fun normalRoundTo(round: NormalRound, userId: UUID): NormalRoundTO = NormalRoundTO(
        round.roundNumber,
        round.presidentialCandidate,
        round.chancellor,
        userId in round.leadershipVotingRound.votes.keys,
        if (userId == round.presidentialCandidate) {
            round.presidentPolicyTiles
        } else {
            emptyList()
        },
        if (userId == round.chancellor) {
            round.chancellorPolicyTiles
        } else {
            emptyList()
        },
        round.executiveAction,
        peekedTiles(round, userId),
        investigationResult(round, userId),
        round.executedPlayer
    )

    private fun peekedTiles(round: NormalRound, userId: UUID) =
        if (round.presidentialCandidate == userId) round.peekedTiles
        else emptyList()

    private fun investigationResult(round: NormalRound, userId: UUID) =
        if (round.presidentialCandidate == userId && round.investigatedPlayerId != null) {
            InvestigationResult(
                round.investigatedPlayerId!!,
                if (round.investigatedPlayerId in game!!.state.liberals) PolicyTile.LIBERAL else PolicyTile.FASCIST
            )
        } else {
            null
        }
}
