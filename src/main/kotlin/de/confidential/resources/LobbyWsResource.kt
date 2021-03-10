package de.confidential.resources

import java.util.*
import javax.websocket.server.ServerEndpoint

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import javax.enterprise.context.ApplicationScoped
import javax.websocket.*
import javax.websocket.server.PathParam

@ApplicationScoped
@ServerEndpoint("/lobby/{userId}")
class LobbyWsResource {

    var sessions: MutableMap<String, Session> = ConcurrentHashMap<String, Session>()

    @OnOpen
    fun onOpen(session: Session, @PathParam("userId") userId: String) {
        println("open $userId")
        sessions.put(userId, session);
        broadcast("$userId session started");
    }

    @OnClose
    fun onClose(session: Session?, @PathParam("userId") userId: String) {
        println("close $userId")
        sessions.remove(userId)
        broadcast("$userId session closed")
    }

    @OnError
    fun onError(session: Session?, @PathParam("userId") userId: String, throwable: Throwable) {
        println("error $userId")
        sessions.remove(userId);
        broadcast("$userId session error")
    }

    @OnMessage
    fun onMessage(message: String, @PathParam("userId") userId: String) {
        println("message $userId")
        broadcast(">> $userId: $message")
    }

    private fun broadcast(message: String) {
        println(sessions.size)
        sessions.values.forEach(Consumer<Session> { s: Session ->
            s.getAsyncRemote().sendObject(message) { result ->
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException())
                }
            }
        })
    }

}
