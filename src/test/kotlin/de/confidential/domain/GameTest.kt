package de.confidential.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

internal class GameTest {
    @Test
    fun initiallyHas6LiberalAnd11FascistTiles() {
        val game = Game(createUsers(5))

        val fascistCount = game.policyTiles.count { tile -> tile == PolicyTile.FASCIST }
        val liberalCount = game.policyTiles.count { tile -> tile == PolicyTile.LIBERAL }

        assertThat(fascistCount).isEqualTo(11)
        assertThat(liberalCount).isEqualTo(6)
    }

    @Test
    fun newGame_startsWith0EnactedPolicies() {
        val game = Game(createUsers(5))

        assertThat(game.enactedFasistPolicies).isEqualTo(0)
        assertThat(game.enactedLiberalPolicies).isEqualTo(0)
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

        assertThat(game.hitler in game.fascists)
    }

    @Test
    fun firstRound_firstSeatBecomesPresidentialCandidate() {
        val game = Game(createUsers(5))

        assertThat(game.presidentialCandidate())
            .isEqualTo(game.seats[0])
    }

    @Test
    fun firstRound_chancellorInitallyNull() {
        val game = Game(createUsers(5))

        assertThat(game.chancellor())
            .isNull()
    }

    @Test
    fun nominateChancellorWorks() {
        val game = Game(createUsers(5))
        assertThat(game.chancellor()).isNull()

        game.nominateChancellor(game.seats[1])

        assertThat(game.chancellor()).isEqualTo(game.seats[1])
    }

    @Test
    fun nominateChancellor_throwsExceptionIfNomineeIsPresidentialCandidate() {
        val game = Game(createUsers(5))
        assertThat(game.chancellor()).isNull()

        assertThrows(IllegalArgumentException::class.java) { ->
            game.nominateChancellor(game.seats[0])
        }
    }

    @Test
    fun voteLeadership_majorityVotesYes_doesNotStartNextRound() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.seats[1])

        users.forEach { u -> game.voteLeadership(u, Vote.YES) }

        assertThat(game.leadershipVoteResult())
            .isEqualTo(VoteResult(5, 0))
    }

    @Test
    fun voteLeadership_majorityVotesNo_startsNextRound() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.seats[1])

        users.forEach { u -> game.voteLeadership(u, Vote.NO) }

        assertThat(game.leadershipVoteResult())
            .isNull();
        assertThat((game.rounds[game.rounds.size - 2] as NormalRound).leadershipVotingRound.voteResult)
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

        game.nominateChancellor(game.seats[1])

        assertThat(game.chancellor())
            .isEqualTo(game.seats[1])
    }

    @Test
    fun nominateChancellor_throwsException_whenChancellorAlreadyNominated() {
        val game = Game(createUsers(5))
        game.nominateChancellor(game.seats[1])

        assertThrows(IllegalArgumentException::class.java) { -> game.nominateChancellor(game.seats[1]) }
    }

    @Test
    fun voteLeadership_registersFirstVote() {
        val game = Game(createUsers(5))
        game.nominateChancellor(game.seats[1])

        game.voteLeadership(game.seats[0], Vote.YES)

        assertThat(game.currentRound.leadershipVotingRound.votes)
            .isEqualTo(mapOf(Pair(game.seats[0],Vote.YES)))
    }

    @Test
    fun voteLeadership_votingTwice_throwsException() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.seats[1])

        game.voteLeadership(users[0], Vote.YES)

        assertThrows(AlreadyVotedException::class.java) { ->
            game.voteLeadership(users[0], Vote.NO)
        }
    }

    @Test
    fun voteLeadership_voteFromNonPlayer_throwsException() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.seats[1])

        game.voteLeadership(game.seats[0], Vote.YES)

        assertThrows(NonPlayersCannotVoteException::class.java) { ->
            game.voteLeadership(User(randomUUID(), "intruder"), Vote.NO)
        }
    }

    @Test
    fun onThreeFailedLeadershipVotes_chaosRoundIsHeldAndNextRoundIsStarted() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.seats[1])

        users.forEach { u -> game.voteLeadership(u, Vote.NO) }
        users.forEach { u -> game.voteLeadership(u, Vote.NO) }
        users.forEach { u -> game.voteLeadership(u, Vote.NO) }

        assertThat(game.currentRound.roundNumber)
            .isEqualTo(2)
    }

    @Test
    fun onFailedVoteLeadership_electionTracker_increasesByOne() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.seats[1])

        users.forEach { u -> game.voteLeadership(u, Vote.NO) }

        assertThat(game.electionTracker.failedElections)
            .isEqualTo(1)
    }

    @Test
    fun onFailedVoteLeadership_givenThreeFailedElections_addsChaosRound() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.seats[1])

        users.forEach { u -> game.voteLeadership(u, Vote.NO) }
        assertThat(game.electionTracker.failedElections)
            .isEqualTo(1)
        assertThat(game.rounds.size == 2)

        users.forEach { u -> game.voteLeadership(u, Vote.NO) }
        assertThat(game.electionTracker.failedElections)
            .isEqualTo(2)
        assertThat(game.rounds.size == 3)

        users.forEach { u -> game.voteLeadership(u, Vote.NO) }
        assertThat(game.electionTracker.failedElections)
            .isEqualTo(0)
        assertThat(game.rounds.size == 5)

        assertThat(game.electionTracker.failedElections)
            .isEqualTo(0)
    }

    @Test
    fun chaosRound_enactsTopNextPolicy() {
        val users = createUsers(5)
        val game = Game(users)
        game.nominateChancellor(game.seats[1])

        val countPolicyTiles = game.policyTiles.size
        val topPolicy = game.policyTiles[0]

        users.forEach { u -> game.voteLeadership(u, Vote.NO) }
        assertThat(game.electionTracker.failedElections)
            .isEqualTo(1)
        users.forEach { u -> game.voteLeadership(u, Vote.NO) }
        assertThat(game.electionTracker.failedElections)
            .isEqualTo(2)
        users.forEach { u -> game.voteLeadership(u, Vote.NO) }

        assertThat(game.electionTracker.failedElections)
            .isEqualTo(0)

        if (topPolicy == PolicyTile.FASCIST) {
            assertThat(game.enactedFasistPolicies)
                .isEqualTo(1)
        }

        if (topPolicy == PolicyTile.LIBERAL) {
            assertThat(game.enactedLiberalPolicies)
                .isEqualTo(1)
        }

        assertThat(game.enactedLiberalPolicies + game.enactedFasistPolicies)
            .isEqualTo(1)
        assertThat(game.policyTiles.size)
            .isEqualTo(countPolicyTiles - 1)
    }

    @Test
    fun voteLeadership_throwsExceptionWhenChancellorNotNominatedYet() {
        val game = Game(createUsers(5))

        assertThrows(IllegalStateException::class.java) { -> game.voteLeadership(game.seats[0], Vote.YES) }
    }

    @Test
    fun voteLeadership_() {
        val game = Game(createUsers(5))

        assertThrows(IllegalStateException::class.java) { -> game.voteLeadership(game.seats[0], Vote.YES) }
    }

    @Test
    fun voteLeadership_givenSuccessfulVote_thenThreePolicyTilesArePresentedToPresident() {
        val game = Game(createUsers(5))
        val countPolicyTiles = game.policyTiles.size
        game.nominateChancellor(game.seats[1])
        game.seats.forEach { u -> game.voteLeadership(u, Vote.YES) }

        assertThat(game.presidentPolicyTiles())
            .hasSize(3)
        assertThat(game.policyTiles.size)
            .isEqualTo(countPolicyTiles - 3)
    }

    @Test
    fun discardPolicyTile_allowsPresidentToDiscardPolicyTile() {
        val game = Game(createUsers(5))
        game.nominateChancellor(game.seats[1])
        game.seats.forEach { u -> game.voteLeadership(u, Vote.YES) }

        val firstPolicy = game.presidentPolicyTiles()!![0]
        game.discardPolicyTile(game.seats[0], firstPolicy)

        assertThat(game.discardedPolicyTiles)
            .containsExactly(firstPolicy)
        assertThat(game.chancellorPolicyTiles())
            .isEqualTo(listOf(game.presidentPolicyTiles()!![1],game.presidentPolicyTiles()!![2]))
    }

    @Test
    fun discardPolicyTile_invalidUsages() {
//        TODO("test discarding too early")
//        TODO("test discarding by wrong person")
//        TODO("test discarding tile that was not presented")
//        TODO("test discarding twice")
    }

    //TODO: test discard by wrong user (first discard must be president, second must be chancellor)

    private fun assertAssignments(playerCount: Int, expectedLiberals: Int, expectedFascists: Int) {
        val game = Game(createUsers(playerCount))
        assertThat(game.liberals).hasSize(expectedLiberals)
        assertThat(game.fascists).hasSize(expectedFascists)

        assertThat((game.liberals + game.fascists).toSet())
            .hasSize(playerCount)
    }

    fun createUsers(n : Int): List<User> {
        return List(n) { i -> User(randomUUID(), "User $i") }
    }
}
