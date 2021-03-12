package de.confidential.resources.ws

import java.util.function.Consumer
import javax.websocket.Session

class TalkHandler(
    val sessions: Map<String, Session>,
    val jsonUtil: JsonUtil
) : LobbyMessageHandler<TalkMessage> {

    override fun canHandle() = TalkMessage::class.toString()

    override fun handle(session: Session, msg: TalkMessage) {
//        println("AAAA")
//        val user = SessionUtil.getUser(session)
//        println("AAAA 2")

        println("aaa")
        broadcast(UserTalkedMessage("bob", msg.message))
        println("bbb")
    }

    private fun broadcast(msg: Any) {
        println("jjj")
        val stringToSend = jsonUtil.toString(msg)
        println("kkk")

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
