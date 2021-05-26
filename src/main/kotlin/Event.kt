import java.util.*

sealed class Event(val id_: String)

data class Enter(val id: String, val date: Date) : Event(id)
data class Exit(val id: String, val date: Date) : Event(id)
data class Subscription(val id: String, val date: Date) : Event(id)


