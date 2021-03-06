package de.confidential.domain

enum class GamePhase {

    NOT_STARTED_YET,
    NOMINATING_CHANCELLOR,
    VOTING_LEADERSHIP,
    PRESIDENT_DISCARDS_POLICY_TILE,
    CHANCELLOR_DISCARDS_POLICY_TILE,
    VETO_REQUESTED,
    PRESIDENT_EXECUTIVE_POWER,
    GAME_OVER
}
