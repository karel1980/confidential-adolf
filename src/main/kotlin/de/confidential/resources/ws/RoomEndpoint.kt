package de.confidential.resources

import com.fasterxml.jackson.annotation.JsonSubTypes
import de.confidential.domain.Room
import de.confidential.domain.RoomRepository
import de.confidential.domain.UserRepository
import de.confidential.resources.ws.*
import io.quarkus.runtime.StartupEvent
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.websocket.*
import javax.websocket.server.PathParam
import javax.websocket.server.ServerEndpoint


@ApplicationScoped
@ServerEndpoint("/ws/room/{roomId}")
class RoomEndpoint {

    @Inject
    @field: Default
    lateinit var jsonUtil: JsonUtil

    @Inject
    @field: Default
    lateinit var comms: Comms

    @Inject
    @field: Default
    lateinit var userRepository: UserRepository

    @Inject
    @field: Default
    lateinit var roomRepository: RoomRepository

    lateinit var roomHandlerMap: Map<String, RoomMessageHandler<*>>

    fun onStart(@Observes ev: StartupEvent?) {
        val handlers = listOf(
            IdentifyHandler(comms, userRepository),
            PingHandler(comms),
            GetRoomStateHandler(comms),
            StartGameHandler(comms),
            GenericRoomMessageHandler(NominateChancellor::class.java),
            GenericRoomMessageHandler(LeadershipVote::class.java),
            GenericRoomMessageHandler(DiscardPolicyTile::class.java),
            GenericRoomMessageHandler(RequestVeto::class.java),
            GenericRoomMessageHandler(ConfirmVeto::class.java),
            GenericRoomMessageHandler(DenyVeto::class.java),
            GenericRoomMessageHandler(InvestigateLoyalty::class.java),
            GenericRoomMessageHandler(CallSpecialElection::class.java),
            GenericRoomMessageHandler(PolicyPeek::class.java),
            GenericRoomMessageHandler(Execution::class.java)

        )
        roomHandlerMap = handlers.map { h -> h.canHandle() to h }.toMap()
    }

    @OnOpen
    fun onOpen(session: Session, @PathParam("roomId") roomId: String) {
        val room = roomRepository.getRoom(UUID.fromString(roomId))
        session.userProperties.put("room", room)
        comms.addSession(room, session)
    }

    @OnClose
    fun onClose(session: Session, @PathParam("roomId") roomId: String) {
        comms.removeSession(session.id)
    }

    @OnError
    fun onError(session: Session, @PathParam("roomId") roomId: String, throwable: Throwable) {
        if (throwable is Feedback) {
            comms.sendDirect(ErrorResponse(throwable::class.toString(), throwable.params), session)
        } else {
            if (session.userProperties["room"] == null) {
                session.close(CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Room not found"))
            }
            println("onError >> $throwable")
            throwable.printStackTrace()
        }
    }

    @OnMessage
    fun handleMessage(session: Session, msgString: String) {
        val msg: IncomingMessage = jsonUtil.toIncoming(msgString)
        val type = msg::class.toString()

        val handler = roomHandlerMap.getOrElse(type) {
            throw RuntimeException("no handler for $type")
        }

        if (!SessionUtil.isIdentified(session) && requiresIdentification(handler)) {
            comms.sendDirect(IdentifyRequest(), session)
            return
        }

        val maybeRoom = session.userProperties["room"] as Room?
        val room = maybeRoom as Room
        handler::class.members.find { it.name == "handle" }!!.call(handler, session, room, msg)

        //TODO: also send event of what happened?
        //comms.sendToAllMembers(createRoomState(room, ), room.id)
        //Send user-specific state to each ember
        comms.sessionsByRoomId[room.id]
            ?.filter { SessionUtil.isIdentified(session) }
            ?.forEach { comms.sendDirect(createRoomState(room, SessionUtil.getUserId(it)), it) }
    }

    private fun createRoomState(room: Room, user: UUID): OutgoingMessage {
        return room.getState(user)
    }

    fun requiresIdentification(handlerRoom: RoomMessageHandler<*>) = handlerRoom.canHandle() == IdentifyRequest::class.toString()

}
