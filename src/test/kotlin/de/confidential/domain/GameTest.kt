package de.confidential.domain

import de.confidential.resources.ws.DiscardPolicyTile
import de.confidential.resources.ws.NominateChancellor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

internal class GameTest {
    @Test
    fun initiallyHas6LiberalAnd11FascistTiles() {
        val game = Game(createUsers(5))

        val fascistCount = game.state.policyTiles.count { tile -> tile == PolicyTile.FASCIST }
        val liberalCount = game.state.policyTiles.count { tile -> tile == PolicyTile.LIBERAL }

        assertThat(fascistCount).isEqualTo(11)
        assertThat(liberalCount).isEqualTo(6)
    }

    @Test
    fun newGame_startsWith0EnactedPolicies() {
        val game = Game(createUsers(5))

        assertThat(game.state.enactedFasistPolicies).isEqualTo(0)
        assertThat(game.state.enactedLiberalPolicies).isEqualTo(0)
    }

    @Test
    fun assignsMembershipsAndRolesOnCreation() {
        assertAssignments(5, 3, 2)
        assertAssignments(6, 4, 2)
        assertAssignments(7, 4, 3)
        assertAssignments(8, 5, 3)
        assertAssignments(9, 5, 4)
        assertAssignments(10, 6, 4)
    }

    @Test
    fun assignsHitler() {
        val game = Game(createUsers(5))

        assertThat(game.state.hitler in game.state.fascists)
    }

    @Test
    fun firstRound_firstSeatBecomesPresidentialCandidate() {
        val game = Game(createUsers(5))

        assertThat(game.presidentialCandidate())
            .isEqualTo(game.state.players[0])
    }

    @Test
    fun firstRound_chancellorInitallyNull() {
        val game = Game(createUsers(5))

        assertThat(game.state.currentRound.chancellor)
            .isNull()
    }

    @Test
    fun nominateChancellorWorks() {
        val players = createUsers(5)
        val game = Game(players)
        assertThat(game.chancellor()).isNull()

        game.on(game.presidentialCandidate().id, NominateChancellor(game.state.players[1].id))

        assertThat(game.chancellor()).isEqualTo(game.state.players[1])
    }

    @Test
    fun nominateChancellor_throwsExceptionIfNomineeIsPresidentialCandidate() {
        val game = Game(createUsers(5))
        assertThat(game.chancellor()).isNull()

        assertThrows(IllegalArgumentException::class.java) {
            game.nominateChancellor(game.state.players[0].id, game.players[0])
        }
    }

    @Test
    fun voteLeadership_majorityVotesYes_doesNotStartNextRound() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.state.players[0].id, game.players[1])

        users.forEach { u -> game.voteLeadership(u.id, Vote.YES) }

        assertThat(game.leadershipVoteResult())
            .isEqualTo(VoteResult(5, 0))
    }

    @Test
    fun voteLeadership_majorityVotesNo_startsNextRound() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.state.players[0].id, game.players[1])

        users.forEach { u ->
            game.voteLeadership(u.id, Vote.NO)
        }

        assertThat(game.leadershipVoteResult())
            .isNull()
        assertThat((game.state.rounds[game.state.rounds.size - 2] as NormalRound).leadershipVotingRound.voteResult)
            .isEqualTo(VoteResult(0, 5))
    }

    @Test
    fun voteResult_givenEqualYesAndNoVotes_thenEndResultIsNo() {
        assertThat(VoteResult(2, 2).endResult)
            .isEqualTo(Vote.NO)
    }

    @Test
    fun nominateChancellor_works() {
        val game = Game(createUsers(5))

        game.nominateChancellor(game.state.players[0].id, game.players[1])

        assertThat(game.chancellor())
            .isEqualTo(game.players[1])
    }

    @Test
    fun nominateChancellor_throwsException_whenChancellorAlreadyNominated() {
        val game = Game(createUsers(5))
        game.nominateChancellor(game.state.players[0].id, game.players[1])

        assertThrows(IllegalArgumentException::class.java) { game.nominateChancellor(game.state.players[0].id, game.players[1]) }
    }

    @Test
    fun voteLeadership_registersFirstVote() {
        val game = Game(createUsers(5))
        game.nominateChancellor(game.state.players[0].id, game.players[1])

        game.voteLeadership(game.players[0].id, Vote.YES)

        assertThat(game.state.currentRound.leadershipVotingRound.votes)
            .isEqualTo(mapOf(Pair(game.players[0], Vote.YES)))
    }

    @Test
    fun voteLeadership_votingTwice_throwsException() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.state.players[0].id, game.players[1])

        game.voteLeadership(users[0].id, Vote.YES)

        assertThrows(AlreadyVotedException::class.java) {
            game.voteLeadership(users[0].id, Vote.NO)
        }
    }

    @Test
    fun voteLeadership_voteFromNonPlayer_throwsException() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.state.players[0].id, game.players[1])

        assertThrows(IllegalArgumentException::class.java) {
            game.voteLeadership(User(randomUUID(), "intruder").id, Vote.NO)
        }
    }

    @Test
    fun onThreeFailedLeadershipVotes_chaosRoundIsHeldAndNextRoundIsStarted() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.state.players[0].id, game.players[1])

        users.forEach { u ->
            game.voteLeadership(u.id, Vote.NO)
        }

        game.nominateChancellor(game.state.players[1].id, game.players[2])
        users.forEach { u ->
            game.voteLeadership(u.id, Vote.NO)
        }

        game.nominateChancellor(game.state.players[2].id, game.players[3])
        users.forEach { u ->
            game.voteLeadership(u.id, Vote.NO)
        }

        assertThat(game.state.currentRound.roundNumber)
            .isEqualTo(2)
    }

    @Test
    fun onFailedVoteLeadership_electionTracker_increasesByOne() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.state.players[0].id, game.players[1])

        users.forEach { u ->
            game.voteLeadership(u.id, Vote.NO)
        }

        assertThat(game.state.electionTracker.failedElections)
            .isEqualTo(1)
    }

    @Test
    fun onFailedVoteLeadership_givenThreeFailedElections_addsChaosRound() {
        val users = createUsers(5)
        val game = Game(users)

        game.nominateChancellor(game.state.players[0].id, game.players[1])
        users.forEach { u ->
            game.voteLeadership(u.id, Vote.NO)
        }
        assertThat(game.state.electionTracker.failedElections)
            .isEqualTo(1)
        assertThat(game.state.rounds.size == 2)

        game.nominateChancellor(game.state.players[1].id, game.players[2])
        users.forEach { u ->
            game.voteLeadership(u.id, Vote.NO)
        }
        assertThat(game.state.electionTracker.failedElections)
            .isEqualTo(2)
        assertThat(game.state.rounds.size == 3)

        game.nominateChancellor(game.state.players[2].id, game.players[3])
        users.forEach { u ->
            game.voteLeadership(u.id, Vote.NO)
        }
        assertThat(game.state.electionTracker.failedElections)
            .isEqualTo(0)
        assertThat(game.state.rounds.size == 5)

        assertThat(game.state.electionTracker.failedElections)
            .isEqualTo(0)
    }

    @Test
    fun chaosRound_enactsTopNextPolicy() {
        val users = createUsers(5)
        val game = Game(users)

        val countPolicyTiles = game.state.policyTiles.size
        val topPolicy = game.state.policyTiles[0]

        game.nominateChancellor(game.state.players[1].id, game.players[2])
        users.forEach { u ->
            game.voteLeadership(u.id, Vote.NO)
        }
        assertThat(game.state.electionTracker.failedElections)
            .isEqualTo(1)

        game.nominateChancellor(game.state.players[1].id, game.players[2])
        users.forEach { u ->
            game.voteLeadership(u.id, Vote.NO)
        }
        assertThat(game.state.electionTracker.failedElections)
            .isEqualTo(2)

        game.nominateChancellor(game.state.players[2].id, game.players[3])
        users.forEach { u ->
            game.voteLeadership(u.id, Vote.NO)
        }
        assertThat(game.state.electionTracker.failedElections)
            .isEqualTo(0)

        if (topPolicy == PolicyTile.FASCIST) {
            assertThat(game.state.enactedFasistPolicies)
                .isEqualTo(1)
        }

        if (topPolicy == PolicyTile.LIBERAL) {
            assertThat(game.state.enactedLiberalPolicies)
                .isEqualTo(1)
        }

        assertThat(game.state.enactedLiberalPolicies + game.state.enactedFasistPolicies)
            .isEqualTo(1)
        assertThat(game.state.policyTiles.size)
            .isEqualTo(countPolicyTiles - 1)
    }

    @Test
    fun voteLeadership_throwsExceptionWhenChancellorNotNominatedYet() {
        val game = Game(createUsers(5))

        assertThrows(IllegalArgumentException::class.java) {
            game.voteLeadership(game.players[0].id, Vote.YES)
        }
    }

    @Test
    fun voteLeadership_givenSuccessfulVote_thenThreePolicyTilesArePresentedToPresident() {
        val game = Game(createUsers(5))
        val countPolicyTiles = game.state.policyTiles.size
        game.nominateChancellor(game.state.players[0].id, game.players[1])
        game.players.forEach { u ->
            game.voteLeadership(u.id, Vote.YES)
        }

        assertThat(game.presidentPolicyTiles())
            .hasSize(3)
        assertThat(game.state.policyTiles.size)
            .isEqualTo(countPolicyTiles - 3)
    }

    @Test
    fun discardPolicyTile_allowsPresidentToDiscardPolicyTile() {
        val game = Game(createUsers(5))
        game.nominateChancellor(game.state.players[0].id, game.players[1])
        game.players.forEach { u ->
            game.voteLeadership(u.id, Vote.YES)
        }

        val selectedTiles = game.presidentPolicyTiles()!!
        val firstPolicy = selectedTiles[0]
        game.discardPolicyTile(game.players[0], firstPolicy)

        assertThat(game.state.discardedPolicyTiles)
            .containsExactly(firstPolicy)
        assertThat(game.chancellorPolicyTiles())
            .isEqualTo(selectedTiles.subList(1, 3))
        assertThat(game.phaseHandler.getPhase())
            .isEqualTo(GamePhase.CHANCELLOR_DISCARDS_POLICY_TILE)
    }

    @Test
    fun discardPolicyTile_throwsExceptionWhenNonPresidentTriesToDiscard() {
        val game = Game(createUsers(5))
        game.nominateChancellor(game.state.players[0].id, game.players[1])
        game.players.forEach { u ->
            game.voteLeadership(u.id, Vote.YES)
        }

        val selectedTiles = game.presidentPolicyTiles()!!
        val firstPolicy = selectedTiles[0]

        assertThrows(IllegalArgumentException::class.java) {
            game.discardPolicyTile(game.players[1], firstPolicy)
        }
    }

    @Test
    fun discardPolicyTile_throwsException_whenCalledBeforeLeaderIsElected() {
        val game = Game(createUsers(5))
        game.nominateChancellor(game.players[0].id, game.players[1])

        assertThrows(IllegalArgumentException::class.java) {
            game.on(game.players[0].id, DiscardPolicyTile(PolicyTile.FASCIST))
        }
    }

    @Test
    fun discardPolicyTile_throwsExceptionWhenDiscardedCardNotPresented() {

    }

    @Test
    fun discardPolicyTile_allowsChancellorToDiscardPolicyTile() {
        val game = Game(createUsers(5))
        game.nominateChancellor(game.state.players[0].id, game.players[1])
        game.players.forEach { u ->
            game.voteLeadership(u.id, Vote.YES)
        }
        val selectedTiles = game.presidentPolicyTiles()!!
        val firstPolicy = selectedTiles[0]
        val secondPolicy = selectedTiles[1]
        game.discardPolicyTile(game.players[0], firstPolicy)

        game.discardPolicyTile(game.players[1], secondPolicy)

        assertThat(game.state.discardedPolicyTiles)
            .containsExactly(firstPolicy, secondPolicy)
        assertThat((game.state.rounds[game.state.rounds.size - 2] as NormalRound).enactedPolicy)
            .isEqualTo(selectedTiles[2])

        assertThat(game.phaseHandler.getPhase())
            .isEqualTo(GamePhase.NOMINATING_CHANCELLOR)
    }

    @Test
    fun discardPolicyTile_byWrongUser_throwsException() {
        val game = Game(createUsers(5))
        game.nominateChancellor(game.state.players[0].id, game.players[1])
        game.players.forEach { u ->
            game.voteLeadership(u.id, Vote.YES)
        }
        val selectedTiles = game.presidentPolicyTiles()!!
        val firstPolicy = selectedTiles[0]
        val secondPolicy = selectedTiles[1]
        assertThrows(IllegalArgumentException::class.javaObjectType) {
            game.discardPolicyTile(game.players[1], firstPolicy)
        }
        game.discardPolicyTile(game.players[0], firstPolicy)
        assertThrows(IllegalArgumentException::class.javaObjectType) {
            game.discardPolicyTile(game.players[2], secondPolicy)
        }
    }

    private fun assertAssignments(playerCount: Int, expectedLiberals: Int, expectedFascists: Int) {
        val game = Game(createUsers(playerCount))
        assertThat(game.state.liberals).hasSize(expectedLiberals)
        assertThat(game.state.fascists).hasSize(expectedFascists)

        assertThat((game.state.liberals + game.state.fascists).toSet())
            .hasSize(playerCount)
    }

    private fun createUsers(n: Int): List<User> {
        return List(n) { i -> User(randomUUID(), "User $i") }
    }
}
