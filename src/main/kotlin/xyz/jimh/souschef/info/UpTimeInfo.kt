package xyz.jimh.souschef.info

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

@Component
class UpTimeInfo : InfoContributor {
    private final val startInstant = Clock.System.now()
    private final val timeZone = TimeZone.currentSystemDefault()
    private final val startTime = startInstant.toLocalDateTime(timeZone)
    override fun contribute(builder: Info.Builder?) {
        require(builder != null) { "builder must not be null" }

        val now = Clock.System.now()
        val uptime = now - startInstant
        builder.withDetails(mapOf("startTime" to "$startTime $timeZone", "uptime" to "$uptime"))
    }
}