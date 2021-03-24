package de.confidential.resources.ws

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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
    JsonSubTypes.Type(value = NominateChancellor::class, name = "NominateChancellor"),
    JsonSubTypes.Type(value = LeadershipVote::class, name = "LeadershipVote"),
    JsonSubTypes.Type(value = DiscardPolicyTile::class, name = "DiscardPolicyTile")
)
interface IncomingMessageMixin
interface IncomingMessage

data class TalkRequest(val message: String) : IncomingMessage
data class IdentifyMessage(val id: UUID?, val name: String): IncomingMessage
class Ping(): IncomingMessage
class GetRoomState(): IncomingMessage
class NominateChancellor(val nominatedChancellorId: UUID): IncomingMessage
class LeadershipVote(val vote: Vote): IncomingMessage
class DiscardPolicyTile(val policyToDiscard: PolicyTile): IncomingMessage

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

class ErrorResponse(val code: String, val data: Any?): OutgoingMessage {
    val error = true
}
class IdentifyRequest: OutgoingMessage
data class IdentificationSuccess(val id: UUID, val name: String): OutgoingMessage
data class RoomCreated(val id: UUID): OutgoingMessage
data class UserAdded(val user: User): OutgoingMessage
class Pong(): OutgoingMessage
data class UserTalked(val user: String, val message: String) : OutgoingMessage
data class RoomState(val users: List<User>): OutgoingMessage

