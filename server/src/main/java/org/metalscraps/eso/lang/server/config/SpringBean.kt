@file:Suppress("unused")

package org.metalscraps.eso.lang.server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Paths

@Configuration
class SpringBean {

    @Bean
    fun getServerConfig() : ServerConfig {
        val config = ServerConfig(Paths.get(".config"))
        config.load(mapOf(
                ServerConfig.ServerConfigOptions.GCP_PROJECT_NAME to "gcp_project_name",
                ServerConfig.ServerConfigOptions.GCP_PROJECT_ZONE to "gcp_project_zone",
                ServerConfig.ServerConfigOptions.GCP_COMPRESS_SERVER_INSTANCE_NAME to "gcp_compress_server_instance_name",
                ServerConfig.ServerConfigOptions.GCP_PERM_JSON_PATH to "/path/to/gcp/perm/json"
        ))
        return config
    }

}