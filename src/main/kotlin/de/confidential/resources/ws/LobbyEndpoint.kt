package de.confidential.resources

import de.confidential.domain.UserRepository
import de.confidential.resources.ws.*
import de.confidential.resources.ws.MessageHandler
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
class LobbyEndpoint {

    @Inject
    @field: Default
    lateinit var jsonUtil: JsonUtil

    @Inject
    @field: Default
    lateinit var userRepository: UserRepository

    val sessions: MutableMap<String, Session> = ConcurrentHashMap<String, Session>()

    lateinit var handlerMap: Map<String, MessageHandler<*>>

    fun onStart(@Observes ev: StartupEvent?) {
        val handlers = listOf(
            IdentifyHandler(sessions, jsonUtil, userRepository),
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
                ErrorResponse(throwable::class.toString(), throwable.params)
            ))
        } else {
            println("onError >> $throwable")
            throwable.printStackTrace()
        }
    }

    @OnMessage
    fun handleMessage(session: Session, msgString: String) {
        println("got message " + msgString)
        val msg: IncomingMessage = jsonUtil.toIncoming(msgString)
        val type = msg::class.toString()

        val handler = handlerMap.getOrElse(type) {
            throw RuntimeException("no handler for $type")
        }

        if (!SessionUtil.isIdentified(session) && requiresIdentification(handler)) {
            session.asyncRemote.sendObject(jsonUtil.asString(IdentifyRequest()))
            return
        }

        handler::class.members.find { it.name == "handle" }!!.call(handler, session, msg)
    }

    fun requiresIdentification(handler: MessageHandler<*>) = handler.canHandle() == IdentifyRequest::class.toString()

}

