package de.confidential.resources.ws

import java.util.function.Consumer
import javax.websocket.Session

class TalkHandler(
    val sessions: Map<String, Session>,
    val jsonUtil: JsonUtil
) : MessageHandler<TalkRequest> {

    override fun canHandle() = TalkRequest::class.toString()

    override fun handle(session: Session, msg: TalkRequest) {
        broadcast(UserTalked("bob", msg.message))
    }

    private fun broadcast(msg: OutgoingMessage) {
        val stringToSend = jsonUtil.asString(msg)

        println("broadcasting >> $stringToSend")
        sessions.values.forEach(Consumer { s: Session ->
            println("lll")
            s.asyncRemote.sendObject(stringToSend) { result ->
                if (result.exception != null) {
                    println("Unable to send message: " + result.exception)
                }
            }
        })
    }
}
