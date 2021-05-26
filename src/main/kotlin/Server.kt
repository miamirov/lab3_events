import io.netty.handler.codec.http.HttpResponseStatus
import io.reactivex.netty.protocol.http.server.HttpServer
import rx.Observable
import java.text.SimpleDateFormat
import java.util.*

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