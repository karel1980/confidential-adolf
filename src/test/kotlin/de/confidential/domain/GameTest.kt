package de.confidential.domain

import de.confidential.domain.PolicyTile.FASCIST
import de.confidential.domain.PolicyTile.LIBERAL
import de.confidential.resources.ws.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

internal class GameTest {
    @Test
    fun initiallyHas6LiberalAnd11FascistTiles() {
        val game = Game(createUsers(5))

        val fascistCount = game.state.policyTiles.count { tile -> tile == FASCIST }
        val liberalCount = game.state.policyTiles.count { tile -> tile == LIBERAL }

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

        if (topPolicy == FASCIST) {
            assertThat(game.state.enactedFasistPolicies)
                .isEqualTo(1)
        }

        if (topPolicy == LIBERAL) {
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
            game.on(game.players[0].id, DiscardPolicyTile(FASCIST))
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

    @Test
    fun chancellorRequestsVetoAndPresidentConfirms_nextRoundStarts() {
        val game = Game(createUsers(5))

        repeat(5) {
            game.state.policyTiles = allFascist()
            voteLeaders(game)
            game.on(game.presidentialCandidate().id, DiscardPolicyTile(game.state.currentRound.presidentPolicyTiles!![0]))
            game.on(game.chancellor()!!.id, DiscardPolicyTile(game.state.currentRound.chancellorPolicyTiles!![0]))
            executeCurrentExecutivePower(game)
        }

        voteLeaders(game)
        game.on(game.presidentialCandidate().id, DiscardPolicyTile(game.state.currentRound.presidentPolicyTiles!![0]))
        game.on(game.chancellor()!!.id, RequestVeto())
        game.on(game.presidentialCandidate().id, ConfirmVeto())

        assertThat(game.phase())
            .isEqualTo(GamePhase.NOMINATING_CHANCELLOR)
        assertThat(game.state.electionTracker.failedElections)
            .isEqualTo(1)
    }

    @Test
    fun executiveAction_policyPeek() {
        val game = Game(createUsers(5))

        // round 0
        game.state.policyTiles = allFascist()
        voteLeaders(game)
        enactPolicy(game)

        // round 1
        game.state.policyTiles = allFascist()
        voteLeaders(game)
        enactPolicy(game)

        // round 2
        game.state.policyTiles = allFascist()
        voteLeaders(game)
        enactPolicy(game)

        assertThat(game.phase())
            .isEqualTo(GamePhase.PRESIDENT_EXECUTIVE_POWER)

        game.on(game.presidentialCandidate().id, PolicyPeek())

        assertThat(game.phase())
            .isEqualTo(GamePhase.NOMINATING_CHANCELLOR)
        assertThat((game.state.rounds[game.state.rounds.size - 2] as NormalRound).performedExecutiveAction)
            .isEqualTo(ExecutivePower.POLICY_PEEK)
    }

    @Test
    fun executiveAction_investigateLoyalty() {
        val game = Game(createUsers(7))

        // round 0
        game.state.policyTiles = allFascist()
        voteLeaders(game)
        enactPolicy(game)

        // round 1
        game.state.policyTiles = allFascist()
        voteLeaders(game)
        enactPolicy(game)

        assertThat(game.phase())
            .isEqualTo(GamePhase.PRESIDENT_EXECUTIVE_POWER)

        game.on(game.presidentialCandidate().id, InvestigateLoyalty(game.chancellor()!!.id))

        assertThat(game.phase())
            .isEqualTo(GamePhase.NOMINATING_CHANCELLOR)
        assertThat((game.state.rounds[game.state.rounds.size - 2] as NormalRound).performedExecutiveAction)
            .isEqualTo(ExecutivePower.INVESTIGATE_LOYALTY)
    }

    @Test
    fun executiveAction_specialElection() {
        val game = Game(createUsers(7))

        // round 0
        game.state.policyTiles = allFascist()
        playFullRound(game)

        // round 1
        game.state.policyTiles = allFascist()
        playFullRound(game)

        // round 2
        game.state.policyTiles = allFascist()
        playFullRound(game)

        assertThat(game.phase())
            .isEqualTo(GamePhase.NOMINATING_CHANCELLOR)
        assertThat(game.presidentialCandidate().id)
            .isEqualTo((game.previousRound() as NormalRound).specialElectionPresidentId)
        assertThat((game.state.rounds[game.state.rounds.size - 2] as NormalRound).performedExecutiveAction)
            .isEqualTo(ExecutivePower.CALL_SPECIAL_ELECTION)
    }

    @Test
    fun executiveAction_execution() {
        val game = Game(createUsers(5))

        // round 0
        game.state.policyTiles = allFascist()
        playFullRound(game)

        // round 1
        game.state.policyTiles = allFascist()
        playFullRound(game)

        // round 2
        game.state.policyTiles = allFascist()
        playFullRound(game)

        // round 3
        game.state.policyTiles = allFascist()
        playFullRound(game)

        assertThat(game.phase())
            .isEqualTo(GamePhase.NOMINATING_CHANCELLOR)
        assertThat((game.state.rounds[game.state.rounds.size - 2] as NormalRound).executedPlayer != null)
    }

    private fun playFullRound(game: Game) {
        voteLeaders(game)
        enactPolicy(game)
        executeCurrentExecutivePower(game)
    }


    private fun enactPolicy(game: Game) {
        game.on(game.presidentialCandidate().id, DiscardPolicyTile(game.presidentPolicyTiles()!![0]))
        game.on(game.chancellor()!!.id, DiscardPolicyTile(game.chancellorPolicyTiles()!![0]))
    }

    private fun voteLeaders(game: Game) {
        game.nominateChancellor(
            game.presidentialCandidate().id,
            game.state.players.first { it.id != game.state.currentRound.presidentialCandidate.id })
        game.players.forEach {
            game.voteLeadership(it.id, Vote.YES)
        }
    }

    private fun executeCurrentExecutivePower(game: Game) {
        val executiveAction = game.state.currentExecutiveAction()
        when (executiveAction) {
            ExecutivePower.INVESTIGATE_LOYALTY ->
                game.on(game.state.currentRound.presidentialCandidate.id, InvestigateLoyalty(game.players[0].id))
            ExecutivePower.CALL_SPECIAL_ELECTION ->
                game.on(game.state.currentRound.presidentialCandidate.id, CallSpecialElection(game.chancellor()!!.id))
            ExecutivePower.POLICY_PEEK ->
                game.on(game.state.currentRound.presidentialCandidate.id, PolicyPeek())
            ExecutivePower.EXECUTION -> {
                val playerToKill = game.players.first {
                    it.id != game.state.hitler.id
                            && it.id != game.presidentialCandidate().id
                            && it.id !in game.state.deadPlayers
                }.id
                game.on(game.state.currentRound.presidentialCandidate.id, Execution(playerToKill))
            }
            else -> {
            }
        }
    }

    @Test
    fun chancellorRequestsVeto_notPossibleIfVetoPowerNotUnlocked() {
        val game = Game(createUsers(5))
        game.nominateChancellor(game.players[0].id, game.players[1])
        game.players.forEach { player -> game.voteLeadership(player.id, Vote.YES) }
        game.discardPolicyTile(game.players[0], game.state.currentRound.presidentPolicyTiles!![0])

        assertThrows(IllegalArgumentException::class.java) {
            game.on(game.players[1].id, RequestVeto())
        }

    }

    private fun assertAssignments(playerCount: Int, expectedLiberals: Int, expectedFascists: Int) {
        val game = Game(createUsers(playerCount))
        assertThat(game.state.liberals).hasSize(expectedLiberals)
        assertThat(game.state.fascists).hasSize(expectedFascists)

        assertThat((game.state.liberals + game.state.fascists).toSet())
            .hasSize(playerCount)
    }

    private fun allFascist(): MutableList<PolicyTile> = mutableListOf(
        FASCIST, FASCIST, FASCIST, FASCIST, FASCIST, FASCIST,
        FASCIST, FASCIST, FASCIST, FASCIST, FASCIST
    )

    private fun createUsers(n: Int): List<User> {
        return List(n) { i -> User(randomUUID(), "User $i") }
    }
}
