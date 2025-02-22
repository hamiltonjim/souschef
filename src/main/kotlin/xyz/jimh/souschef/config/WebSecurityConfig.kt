package xyz.jimh.souschef.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableWebSecurity
class WebSecurityConfig {
    @Value("\${cors.originPatterns:*}")
    val originPatterns: List<String>? = null

    @Bean
    fun configure(http: HttpSecurity): SecurityFilterChain {
        http.cors { it.configurationSource {
            val configuration = CorsConfiguration()
            configuration.allowedOriginPatterns = originPatterns
            configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            configuration.allowedHeaders = listOf("*")
            configuration
        } }
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests{ it.anyRequest().permitAll() }

        return http.build()
    }

}