/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.info

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

/**
 * [InfoContributor] that reports the host, port, and context path for the service.
 */
@Component
class HostInfo : InfoContributor {
    @Value("\${HOST}")
    internal var host: String? = null
    @Value("\${server.port}")
    internal var port: Int? = null
    @Value("\${server.servlet.context-path}")
    internal var contextPath: String? = null

    /**
     * Provides the information named in the class description as a JSON object,
     * to be displayed in URI /souschef/actuator/info
     */
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