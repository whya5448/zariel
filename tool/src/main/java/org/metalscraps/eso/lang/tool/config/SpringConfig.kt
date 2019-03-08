@file:Suppress("unused")

package org.metalscraps.eso.lang.tool.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringConfig {

    @Bean
    fun getObjectMapper() : ObjectMapper {
        return ObjectMapper()
    }

}