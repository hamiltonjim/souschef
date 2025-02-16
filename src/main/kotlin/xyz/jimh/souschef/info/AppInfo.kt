package xyz.jimh.souschef.info

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
@ConfigurationProperties(prefix = "info.application")
class AppInfo {
    var name: String? = null
    var description: String? = null
    var version: String? = null
    var encoding: String? = null
    var kotlin: KotlinInfo = KotlinInfo()
    var java: JavaVersion = JavaVersion()

    class KotlinInfo {
        var version: String? = null
        var codeStyle: String? = null
    }
    class JavaVersion {
        var source: String? = null
        var target: String? = null
    }
}


@Component
class AppInfoContributor(val appInfo: AppInfo) : InfoContributor {

    override fun contribute(builder: Info.Builder?) {
        require(builder != null) { "builder must not be null" }
        val javaMap = mapOf("source" to appInfo.java.source, "target" to appInfo.java.target)
        val kotlinMap = mapOf("version" to appInfo.kotlin.version, "codeStyle" to appInfo.kotlin.codeStyle)
        val map = mapOf(
            "name" to appInfo.name,
            "description" to appInfo.description,
            "version" to appInfo.version,
            "encoding" to appInfo.encoding,
            "kotlin" to kotlinMap,
            "java" to javaMap
        )
        builder.withDetail("application", map)
    }
}
