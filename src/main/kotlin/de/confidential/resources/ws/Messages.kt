package de.confidential.resources.ws

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.confidential.domain.GamePhase
import de.confidential.domain.PolicyTile
import de.confidential.domain.User
import de.confidential.domain.Vote
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TalkRequest::class, name = "Talk"),
    JsonSubTypes.Type(value = IdentifyMessage::class, name = "Identify"),
    JsonSubTypes.Type(value = Ping::class, name = "Ping"),
    JsonSubTypes.Type(value = GetRoomState::class, name = "GetRoomState"),
    JsonSubTypes.Type(value = StartGame::class, name = "StartGame"),
    JsonSubTypes.Type(value = NominateChancellor::class, name = "NominateChancellor"),
    JsonSubTypes.Type(value = LeadershipVote::class, name = "LeadershipVote"),
    JsonSubTypes.Type(value = DiscardPolicyTile::class, name = "DiscardPolicyTile"),
    JsonSubTypes.Type(value = RequestVeto::class, name = "RequestVeto"),
    JsonSubTypes.Type(value = ConfirmVeto::class, name = "ConfirmVeto"),
    JsonSubTypes.Type(value = DenyVeto::class, name = "DenyVeto"),
    JsonSubTypes.Type(value = InvestigateLoyalty::class, name = "InvestigateLoyalty"),
    JsonSubTypes.Type(value = CallSpecialElection::class, name = "CallSpecialElection"),
    JsonSubTypes.Type(value = PolicyPeek::class, name = "PolicyPeek"),
    JsonSubTypes.Type(value = Execution::class, name = "Execution")

)
interface IncomingMessageMixin
interface IncomingMessage

data class TalkRequest(val message: String) : IncomingMessage
data class IdentifyMessage(val id: UUID?, val name: String) : IncomingMessage
class Ping() : IncomingMessage
class GetRoomState() : IncomingMessage
class StartGame() : IncomingMessage
data class NominateChancellor(val nominatedChancellorId: UUID) : IncomingMessage
data class LeadershipVote(val vote: Vote) : IncomingMessage
data class DiscardPolicyTile(val policyToDiscard: PolicyTile) : IncomingMessage
class RequestVeto() : IncomingMessage
class ConfirmVeto() : IncomingMessage
class DenyVeto() : IncomingMessage
data class InvestigateLoyalty(val targetId: UUID) : IncomingMessage
class CallSpecialElection(val nextPresidentId: UUID) : IncomingMessage
class PolicyPeek() : IncomingMessage
class Execution(val targetId: UUID) : IncomingMessage

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = IdentifyRequest::class, name = "Identify"),
    JsonSubTypes.Type(value = IdentificationSuccess::class, name = "IdentificationSuccess"),
    JsonSubTypes.Type(value = UserTalked::class, name = "UserTalked"),
    JsonSubTypes.Type(value = Pong::class, name = "Pong"),
    JsonSubTypes.Type(value = RoomState::class, name = "RoomState")
)
interface OutgoingMessageMixin
interface OutgoingMessage

class ErrorResponse(val code: String, val data: Any?) : OutgoingMessage {
    val error = true
}

class IdentifyRequest : OutgoingMessage
data class IdentificationSuccess(val id: UUID, val name: String) : OutgoingMessage
data class RoomCreated(val id: UUID) : OutgoingMessage
data class UserAdded(val user: User) : OutgoingMessage
class Pong() : OutgoingMessage
data class UserTalked(val user: String, val message: String) : OutgoingMessage

data class RoomState(
    val users: List<User>,
    val game: GameTO?
) : OutgoingMessage

data class GameTO(
    val players: List<PlayerTO>,
    val rounds: List<RoundTO>,
    val liberalPolicies: Int,
    val fascistPolicies: Int,
    val phase: GamePhase,
    val winner: PolicyTile?
)

data class PlayerTO(
    val id: UUID,
    val name: String,
    val dead: Boolean,
    val hitler: Boolean?
)

interface RoundTO {}

data class ChaosRoundTO(
    val roundNumber: Int
) : RoundTO

data class NormalRoundTO(
    val roundNumber: Int,
    val president: UUID,
    val chancellor: UUID?,
    val peekedTiles: List<PolicyTile>?,
    val investigationResult: InvestigationResult?,
    val executedPlayer: UUID?
) : RoundTO

data class InvestigationResult(
    val player: UUID,
    val result: PolicyTile
)
