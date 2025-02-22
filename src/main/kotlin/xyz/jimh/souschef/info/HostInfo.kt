package xyz.jimh.souschef.info

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

@Component
class HostInfo : InfoContributor {
    @Value("\${HOST}")
    var host: String? = null
    @Value("\${server.port}")
    var port: Int? = null
    @Value("\${server.servlet.context-path}")
    var contextPath: String? = null

    override fun contribute(builder: Info.Builder?) {
        require(builder != null) { "builder must not be null" }
        if (host == null) {
            host = System.getenv("HOSTNAME")
        }
        builder.withDetail("hostInfo", mapOf(
            "host" to host,
            "port" to port,
            "contextPath" to contextPath,
            "url" to "http://$host:$port$contextPath"
        ))
    }
}