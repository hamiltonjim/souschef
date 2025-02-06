package xyz.jimh.souschef.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializationFeature
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class MapperConfig(val objectMapper: ObjectMapper) {
//    @PostConstruct
//    fun init() {
//        val serializationConfig = objectMapper.serializationConfig.without(SerializationFeature.FAIL_ON_EMPTY_BEANS)
//        objectMapper.setConfig(serializationConfig)
//    }
}