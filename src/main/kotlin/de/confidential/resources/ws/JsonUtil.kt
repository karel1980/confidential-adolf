package de.confidential.resources.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import javax.inject.Singleton

@Singleton
class JsonUtil {

    val mapper: ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule())
        .registerModule(
            SimpleModule("lobby")
                .setMixInAnnotation(IncomingMessage::class.java, IncomingMessageMixin::class.java)
                .setMixInAnnotation(OutgoingMessage::class.java, OutgoingMessageMixin::class.java)
        )

    fun asString(value: OutgoingMessage): String = mapper.writeValueAsString(value)
    fun toIncoming(value: String): IncomingMessage = mapper.readValue(value, IncomingMessage::class.java)

}
