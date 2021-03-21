package de.confidential.domain

class ElectionTracker {

    var failedElections = 0;

    fun increaseFailedElections() {
        failedElections += 1
    }

    fun resetFailedElections() {
        failedElections = 0
    }

    override fun toString(): String {
        return "ElectionTracker(failedElections=$failedElections) ${super.toString()}"
    }

}
