package de.confidential.resources

import de.confidential.domain.Room
import de.confidential.domain.RoomRepository
import de.confidential.domain.UserRepository
import de.confidential.resources.ws.*
import io.quarkus.runtime.StartupEvent
import java.lang.reflect.InvocationTargetException
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
                GenericGameMessageHandler(NominateChancellor::class.java),
                GenericGameMessageHandler(LeadershipVote::class.java),
                GenericGameMessageHandler(DiscardPolicyTile::class.java),
                GenericGameMessageHandler(RequestVeto::class.java),
                GenericGameMessageHandler(ConfirmVeto::class.java),
                GenericGameMessageHandler(DenyVeto::class.java),
                GenericGameMessageHandler(InvestigateLoyalty::class.java),
                GenericGameMessageHandler(CallSpecialElection::class.java),
                GenericGameMessageHandler(PolicyPeek::class.java),
                GenericGameMessageHandler(Execution::class.java),
                RestartGameHandler()
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
            } else {
                comms.sendDirect(ErrorResponse(throwable::class.toString(), throwable.message), session)
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

        val room = session.userProperties["room"] as Room
        synchronized(room) {
            try {
                handler::class.members.find { it.name == "handle" }!!.call(handler, session, room, msg)
            } catch (e: InvocationTargetException) {
                println("There was an error - won't send new state to room members")
                throw if (e.cause == null) e else e.cause!!
            }

            //TODO: also send event of what happened?
            println("handled $type - $msg")
            println("Sending room state to every member")
            comms.sessionsByRoomId[room.id]
                    ?.filter { SessionUtil.isIdentified(session) }
                    ?.forEach {
                        val userId = SessionUtil.getUserId(it)
                        println("Sending room state to $userId")
                        comms.sendDirect(createRoomState(room, userId), it)
                    }
        }
    }

    private fun createRoomState(room: Room, user: UUID): OutgoingMessage {
        return room.getState(user)
    }

    fun requiresIdentification(handlerRoom: RoomMessageHandler<*>) = handlerRoom.canHandle() == IdentifyRequest::class.toString()

}
