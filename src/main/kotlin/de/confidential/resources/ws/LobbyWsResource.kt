package de.confidential.resources

import de.confidential.resources.ws.*
import io.quarkus.runtime.StartupEvent
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.websocket.*
import javax.websocket.server.ServerEndpoint


@ApplicationScoped
@ServerEndpoint("/ws/lobby")
class LobbyWsResource {

    @Inject
    @field: Default
    lateinit var jsonUtil: JsonUtil

    val sessions: MutableMap<String, Session> = ConcurrentHashMap<String, Session>()

    lateinit var handlerMap: Map<String, LobbyMessageHandler<*>>

    fun onStart(@Observes ev: StartupEvent?) {
        val handlers = listOf(
            IdentifyHandler(sessions),
            TalkHandler(sessions, jsonUtil)
        )
        handlerMap = handlers.map { h -> h.canHandle() to h }.toMap()
    }

    @OnOpen
    fun onOpen(session: Session) {
        sessions.put(session.id, session)
    }

    @OnClose
    fun onClose(session: Session) {
        sessions.remove(session.id)
    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {
        if (throwable is Feedback) {
            session.asyncRemote.sendObject(jsonUtil.mapper.writeValueAsString(
                //TODO: add mixin
                ErrorResponse(throwable::class.toString(), throwable.params)
            ))
        } else {
            println("onError >> $throwable")
            throwable.printStackTrace()
        }
    }

    data class ErrorResponse(var type: String, val data: Any?) {
        val error = true
    }

    @OnMessage
    fun handleMessage(session: Session, msgString: String) {
        val msg: LobbyMessage = jsonUtil.toLobbyMessage(msgString)
        val type = msg::class.toString()

        val handler = handlerMap.getOrElse(type) {
            throw RuntimeException("no handler for $type")
        }

        handler::class.members.find { it.name == "handle" }!!.call(handler, session, msg)
    }

}

