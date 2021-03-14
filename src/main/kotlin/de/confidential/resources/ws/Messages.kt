package de.confidential.resources.ws

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TalkRequest::class, name = "Talk"),
    JsonSubTypes.Type(value = IdentifyMessage::class, name = "Identify"),
    JsonSubTypes.Type(value = CreateGameMessage::class, name = "CreateGame")
)
interface IncomingMessageMixin
interface IncomingMessage

class TalkRequest(val message: String) : IncomingMessage
class IdentifyMessage(val id: UUID?, val name: String): IncomingMessage
class CreateGameMessage(val isPrivate: Boolean) : IncomingMessage

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = IdentifyRequest::class, name = "Identify"),
    JsonSubTypes.Type(value = IdentificationSuccess::class, name = "IdentificationSuccess"),
    JsonSubTypes.Type(value = UserTalkedMessage::class, name = "CreateGame")
)
interface OutgoingMessageMixin
interface OutgoingMessage

class ErrorResponse(val code: String, val data: Any?): OutgoingMessage {
    val error = true
}
class IdentifyRequest: OutgoingMessage
data class IdentificationSuccess(val id: UUID, val name: String): OutgoingMessage
data class UserTalkedMessage(val user: String, val message: String) : OutgoingMessage


