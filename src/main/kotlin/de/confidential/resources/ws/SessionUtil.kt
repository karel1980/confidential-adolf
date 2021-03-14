package de.confidential.resources.ws

import java.util.*
import javax.websocket.Session

class SessionUtil {
    companion object {

        fun getUserId(session: Session): UUID {
            return session.userProperties.getOrElse("USER_ID") {
                throw IdentificationRequiredException()
            } as UUID
        }

        fun isIdentified(session: Session): Boolean {
            return session.userProperties.containsKey("USER_ID")
        }

        fun identify(session: Session, user: UUID) {
            session.userProperties["USER_ID"] = user
        }
    }
}
