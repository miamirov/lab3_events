import java.util.*

class ManagerServer(val eventStorage: EventStorage) {

    fun getSubscriptionDate(id: String): Date? {
        return (eventStorage.events.lastOrNull { it is Subscription && it.id == id } as Subscription?)?.date
    }

    fun addSubscription(id: String, date: Date) {
        eventStorage.addEvent(Subscription(id, date))
    }
}