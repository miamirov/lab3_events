import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class TestEventSource {
    private lateinit var eventStorage: EventStorage
    private lateinit var managerServer: ManagerServer
    private lateinit var reportService: ReportService
    private lateinit var turnstyleService: TurnStyleService


    @Before
    fun clear() {
        eventStorage = EventStorage()
        managerServer = ManagerServer(eventStorage)
        turnstyleService = TurnStyleService(eventStorage)

        reportService = ReportService()
        eventStorage.reportService = reportService

    }

    @Test
    fun testSimpleManager() {
        managerServer.addSubscription("1", Date(1, 2, 3))

        assertEquals(managerServer.getSubscriptionDate("1"), Date(1, 2, 3))
        assertEquals(managerServer.getSubscriptionDate("2"), null)
        assertEquals(eventStorage.events.size, 1)
    }

    @Test
    fun testSimpleTurnstyle() {
        managerServer.addSubscription("1", Date(100, 200, 300))

        val a = turnstyleService.tryToEnter("1", Date(1, 2, 3))
        val b = turnstyleService.tryToExit("1", Date(1, 2, 4))

        assertEquals(a, "Success enter")
        assertEquals(b, "Success exit")
    }

    @Test
    fun testSimpleReport() {
        eventStorage.addEvent(Enter("11", Date(1, 2, 2)))
        eventStorage.addEvent(Exit("11", Date(1, 2, 3)))
        assertEquals(reportService.average(), 864e5)
    }
}