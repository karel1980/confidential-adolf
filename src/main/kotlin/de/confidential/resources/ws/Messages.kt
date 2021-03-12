package de.confidential.resources.ws

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TalkMessage::class, name = "Talk"),
    JsonSubTypes.Type(value = IdentifyMessage::class, name = "Identify"),
    JsonSubTypes.Type(value = CreateGameMessage::class, name = "CreateGame")
)
interface LobbyMessageMixin
interface LobbyMessage
class TalkMessage(val message: String) : LobbyMessage
class UserTalkedMessage(val user: String, val message: String) : LobbyMessage
class IdentifyMessage(val token: UUID): LobbyMessage
class CreateGameMessage(val isPrivate: Boolean) : LobbyMessage

