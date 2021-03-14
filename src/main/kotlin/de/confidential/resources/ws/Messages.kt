package de.confidential.resources.ws

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TalkRequest::class, name = "Talk"),
    JsonSubTypes.Type(value = IdentifyMessage::class, name = "Identify"),
    JsonSubTypes.Type(value = CreateRoom::class, name = "CreateRoom")
)
interface IncomingMessageMixin
interface IncomingMessage

class TalkRequest(val message: String) : IncomingMessage
class IdentifyMessage(val id: UUID?, val name: String): IncomingMessage
class CreateRoom(val isPrivate: Boolean) : IncomingMessage

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = IdentifyRequest::class, name = "Identify"),
    JsonSubTypes.Type(value = IdentificationSuccess::class, name = "IdentificationSuccess"),
    JsonSubTypes.Type(value = UserTalked::class, name = "UserTalked")
)
interface OutgoingMessageMixin
interface OutgoingMessage

class ErrorResponse(val code: String, val data: Any?): OutgoingMessage {
    val error = true
}
class IdentifyRequest: OutgoingMessage
data class IdentificationSuccess(val id: UUID, val name: String): OutgoingMessage
data class RoomCreated(val id: UUID): OutgoingMessage
data class UserTalked(val user: String, val message: String) : OutgoingMessage


