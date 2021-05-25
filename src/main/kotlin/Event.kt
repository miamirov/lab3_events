import io.netty.handler.codec.http.HttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.reactivex.netty.protocol.http.server.HttpServer
import rx.Observable
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

sealed class Event(val id_: String)

data class Enter(val id: String, val date: Date) : Event(id)
data class Exit(val id: String, val date: Date) : Event(id)
data class Subscription(val id: String, val date: Date) : Event(id)

class EventStorage {
    val events: MutableList<Event> = mutableListOf()
    lateinit var reportService: ReportService


    fun addEvent(event: Event) {
        events.add(event)
        if ((event is Exit) or (event is Enter))
            reportService.addEvent(event)
    }

}


class ReportService {

    var sum: Long = 0
    var sumTime: Long = 0
    var enters = mutableMapOf<String, Date>()
    var visits = mutableMapOf<String, Int>()

    fun addEvent(event: Event) {
        print(1)
        if (event is Exit) {
            val time = event.date.time - enters[event.id]!!.time
            sum += 1
            sumTime += time
            enters.remove(event.id)
        } else {
            val date: String = SimpleDateFormat("yyyy-dd-mm").format((event as Enter).date)
            visits.putIfAbsent(date, 0)
            visits[date] = visits[date]!! + 1
            enters[event.id] = event.date
        }

    }

    fun average() = if (sum == 0L) 0 else sumTime.toDouble() / sum


}

class ManagerServer(val eventStorage: EventStorage) {

    fun getSubscriptionDate(id: String): Date? {
        return (eventStorage.events.lastOrNull { it is Subscription && it.id == id } as Subscription?)?.date
    }

    fun addSubscription(id: String, date: Date) {
        eventStorage.addEvent(Subscription(id, date))
    }
}

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

fun main() {
    val eventStorage = EventStorage()
    val managerServer = ManagerServer(eventStorage)
    val turnstyleService = TurnStyleService(eventStorage)

    val reportService = ReportService()
    eventStorage.reportService = reportService
    HttpServer.newServer(8080).start { req, resp ->
        val path = req.decodedPath
        val params = req.queryParameters
        val (status, response) = when (path) {
            "/enter" -> {
                val id = params["id"]!![0]
                val date = SimpleDateFormat("yyyy-mm-dd").parse(params["date"]!![0])
                HttpResponseStatus.OK to Observable.just(turnstyleService.tryToEnter(id, date))
            }
            "/exit" -> {
                val id = params["id"]!![0]
                val date = SimpleDateFormat("yyyy-mm-dd").parse(params["date"]!![0])
                HttpResponseStatus.OK to Observable.just(turnstyleService.tryToExit(id, date))
            }
            "/average" -> {
                HttpResponseStatus.OK to Observable.just(reportService.average().toString())
            }
            "/subscribe" -> {
                val id = params["id"]!![0]
                val date = SimpleDateFormat("yyyy-mm-dd").parse(params["date"]!![0])
                managerServer.addSubscription(id, date)
                HttpResponseStatus.OK to Observable.just("Subscription success")
            }
            "/getSub" -> {
                val id = params["id"]!![0]
                val sub: Date? = managerServer.getSubscriptionDate(id)
                val str: String = if (sub == null) "No sub" else SimpleDateFormat("yyyy-dd-mm").format((sub))
                HttpResponseStatus.OK to Observable.just(str)
            }
            else -> HttpResponseStatus.BAD_GATEWAY to Observable.just("Bad request")

        }
        resp.status = status
        resp.writeString(response)
    }.awaitShutdown()
}