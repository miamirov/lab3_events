import java.util.*

class TurnStyleService(val eventStorage: EventStorage) {

    fun tryToEnter(id: String, time: Date): String {
        val last = lastEvent(id)
        if (last is Enter)
            return "Can;t enter, user inside"
        val subcribeUntil = subscriptionDate(id)
        if (subcribeUntil == null || subcribeUntil.time < time.time)
            return "Can't enter no subscription"
        eventStorage.addEvent(Enter(id, time))
        return "Success enter"
    }

    fun lastEvent(id: String): Event? {
        return eventStorage.events.lastOrNull { (it is Enter || it is Exit) && it.id_ == id }
    }

    fun tryToExit(id: String, time: Date): String {
        val last = lastEvent(id)
        if (last is Enter) {
            eventStorage.addEvent(Enter(id, time))
            return "Success exit"
        }
        return "Cant enter, user outside"
    }

    fun subscriptionDate(id: String): Date? =
        (eventStorage.events.lastOrNull { it is Subscription && it.id == id } as Subscription?)?.date

}