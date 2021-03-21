package de.confidential.domain

import java.lang.RuntimeException

class NonPlayersCannotVoteException: RuntimeException("non-players cannot vote") {
}
