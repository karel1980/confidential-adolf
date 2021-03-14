package de.confidential.resources.ws

import javax.websocket.Session

interface MessageHandler<M : IncomingMessage> {

    fun canHandle(): String

    fun handle(session: Session, msg: M): Any
}
