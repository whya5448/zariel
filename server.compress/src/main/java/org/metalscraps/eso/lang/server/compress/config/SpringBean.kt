@file:Suppress("unused")

package org.metalscraps.eso.lang.server.compress.config

import org.metalscraps.eso.lang.lib.config.ESOConfigOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Paths

@Configuration
class SpringBean {

    @Bean
    fun getConfig(): CompressServerConfig {
        val config = CompressServerConfig(Paths.get(".config"))
        config.load(mapOf(
                CompressServerConfig.CompressServerConfigOptions.MAIN_SERVER_HOST_NAME to "hostname",
                CompressServerConfig.CompressServerConfigOptions.MAIN_SERVER_PORT to "22",
                CompressServerConfig.CompressServerConfigOptions.MAIN_SERVER_USER_ID to "username",
                CompressServerConfig.CompressServerConfigOptions.MAIN_SERVER_PO_PATH to "/path/to/po",
                CompressServerConfig.CompressServerConfigOptions.MAIN_SERVER_VERSION_DOCUMENT_PATH to "/path/to/doc",
                CompressServerConfig.CompressServerConfigOptions.WORK_DIR to "."
        ).toMap(HashMap<ESOConfigOptions, Any>()))
        return config
    }


}