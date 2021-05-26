class EventStorage {
    val events: MutableList<Event> = mutableListOf()
    lateinit var reportService: ReportService


    fun addEvent(event: Event) {
        events.add(event)
        if ((event is Exit) or (event is Enter))
            reportService.addEvent(event)
    }

}