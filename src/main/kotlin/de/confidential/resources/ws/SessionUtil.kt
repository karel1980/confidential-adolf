package de.confidential.resources.ws

import javax.websocket.Session

class SessionUtil {
    companion object {

        fun getUser(session: Session): String {
            return session.userProperties.getOrElse("USER_ID") {
                throw IdentificationRequiredException()
            } as String
        }

        fun identify(session: Session, user: String) {
            session.userProperties["USER_ID"] = user
        }
    }
}
