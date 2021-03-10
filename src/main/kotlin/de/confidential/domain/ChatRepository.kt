package de.confidential.domain

import java.time.LocalDateTime
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ChatRepository {

    val queue = FixedSizeQueue<Message>();
    fun getLast10() = queue.getAll()
    fun send(m: Message) = queue.add(m)

}

class FixedSizeQueue<T> {
    val size = 10;
    var items = mutableListOf<T>()
    var position = 0;

    fun getAll() = items.subList(position, items.size) + items.subList(0, position)

    fun add(item: T) {
        if (items.size < size) {
            items.add(item)
        } else {
            items.set(position, item)
        }
        position = (position + 1) % size
    }
}

data class Message(val user: String, val time: LocalDateTime, val message: String);
