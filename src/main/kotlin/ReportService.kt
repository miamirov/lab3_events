import java.text.SimpleDateFormat
import java.util.*

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